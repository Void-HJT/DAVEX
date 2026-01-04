package DavexAgent.module.verdict;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.entity.File;
import DavexBase.info.VerdictFilterDTO;
import DavexBase.mapper.FileMapper;
import DavexBase.service.directory.FileFolderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VerdictService {
    // 日志记录（便于问题排查）
    private static final Logger logger = LoggerFactory.getLogger(VerdictService.class);

    // 配置参数（可根据实际需求调整，如输出前缀、缩放系数）
    private static final String PY_SCRIPT_NAME = "p1preprocess.py"; // Python脚本文件名
//    private static final String OUTPUT_PREFIX = "dataset_embeddings"; // 输出文件前缀（对应Python脚本的--prefix参数）
    private static final long TIMEOUT_MINUTES = 30; // 脚本执行超时时间（防止无限阻塞）
    // 新增：用于提取D后面数字的正则表达式
    private static final String D_NUM_PATTERN = "D(\\d+)";

    @Autowired
    private My my;
    @Autowired
    private FileFolderService fileFolderService;
    @Autowired
    private FileMapper fileMapper;

    /**
     * 按条件筛选判决书文件ID
     * @param filterDTO 筛选条件（可为null，null表示仅按基础条件筛选）
     * @return 符合条件的fileId（即File.uid）列表
     */
    public Body<List<String>> getDestIds(VerdictFilterDTO filterDTO) {
        try {
            QueryWrapper<File> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("judge_time");

            if (filterDTO != null) {
                if (filterDTO.getJudgeTimeStart() != null) {
                    queryWrapper.ge("judge_time", filterDTO.getJudgeTimeStart());
                }
                if (filterDTO.getJudgeTimeEnd() != null) {
                    queryWrapper.le("judge_time", filterDTO.getJudgeTimeEnd());
                }
                if (filterDTO.getJudgeType() != null && !filterDTO.getJudgeType().trim().isEmpty()) {
                    queryWrapper.like("judge_type", filterDTO.getJudgeType().trim());
                }
                if (filterDTO.getJudgeDistrict() != null && !filterDTO.getJudgeDistrict().trim().isEmpty()) {
                    queryWrapper.like("judge_district", filterDTO.getJudgeDistrict().trim());
                }
                if (filterDTO.getJudgeCause() != null && !filterDTO.getJudgeCause().trim().isEmpty()) {
                    queryWrapper.like("judge_cause", filterDTO.getJudgeCause().trim());
                }
            }

            List<File> fileList = fileMapper.selectList(queryWrapper.select("uid"));
            List<String> fileIdList = fileList.stream()
                    .map(File::getUid)
                    .collect(Collectors.toList());

            logger.info("筛选完成，共匹配 {} 个文件ID", fileIdList.size());
            return Body.success(fileIdList, "筛选成功");

        } catch (Exception e) {
            logger.error("筛选文件ID失败", e);
            return Body.error("筛选文件ID异常：" + e.getMessage());
        }
    }

    /**
     * 生成数据集Embeddings并返回vectorizer.pkl文件路径
     * @param fileIds 待处理的文件ID列表
     * @param outputPrefix 输出文件前缀（新增参数，替代原有硬编码常量）
     * @return 成功：Body.data为pkl文件绝对路径；失败：Body包含错误信息
     */
    public Body<String> getDataEmbeddings(List<String> fileIds, String outputPrefix) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Body.error("文件ID列表不能为空");
        }
        // 新增：校验输出前缀参数
        if (outputPrefix == null || outputPrefix.trim().isEmpty()) {
            return Body.error("输出文件前缀不能为空");
        }

        String corePath = my.getCore_path();
        if (corePath == null || corePath.trim().isEmpty()) {
            return Body.error("系统Core路径未配置");
        }
        String pyScriptPath = Paths.get(corePath, PY_SCRIPT_NAME).toString();
        if (!Files.exists(Paths.get(pyScriptPath))) {
            return Body.error("数据集处理脚本不存在：" + pyScriptPath);
        }

        // 原有文件路径、数字ID收集逻辑不变...
        StringJoiner filePathsJoiner = new StringJoiner(" ");
        StringJoiner fileNumIdsJoiner = new StringJoiner(" ");
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(D_NUM_PATTERN);
        for (String fileId : fileIds) {
            File file = fileMapper.selectById(fileId);
            if (file == null) {
                logger.warn("文件ID {} 不存在，已跳过", fileId);
                continue;
            }
            String filePath = fileFolderService.getFilePath(file, my.getBase_path());
            if (filePath == null || !Files.exists(Paths.get(filePath))) {
                logger.warn("文件路径无效：fileId={}, filePath={}", fileId, filePath);
                continue;
            }
            filePathsJoiner.add("\"" + filePath + "\"");

            java.util.regex.Matcher matcher = pattern.matcher(fileId);
            if (matcher.find()) {
                String numId = matcher.group(1);
                fileNumIdsJoiner.add(numId);
            } else {
                logger.warn("文件ID {} 格式异常，无法提取D后数字，已跳过", fileId);
                continue;
            }
        }

        String filePathsStr = filePathsJoiner.toString();
        String fileNumIdsStr = fileNumIdsJoiner.toString();
        if (filePathsStr.trim().isEmpty()) {
            return Body.error("所有文件ID对应路径无效，无可用文件");
        }
        if (fileNumIdsStr.trim().isEmpty()) {
            return Body.error("所有文件ID格式异常，无法提取有效数字ID");
        }

        // 命令拼接：修改Python脚本的-p参数，指向任务专属文件夹（文件名不变）
        String outputRootDir = Paths.get(corePath, "output").toString(); // 仅保留输出根目录
        String pythonPath = "/home/zkx/miniconda3/bin/python";
        String fullOutputPrefix = Paths.get(outputRootDir, outputPrefix).toString();
        String fullCmd = String.format(
                "%s \"%s\" %s --file-ids %s -p \"%s\"",
                pythonPath,
                pyScriptPath,
                filePathsStr,
                fileNumIdsStr,
                fullOutputPrefix // 传递完整路径前缀，而非纯outputPrefix
        );
        logger.info("执行命令：{}", fullCmd);

        // 关键修改2：（可选，保持原有逻辑不变，此处已匹配Python脚本输出）
        String pklFileName = outputPrefix + "_vectorizer.pkl";
        String pklFilePath = Paths.get(fullOutputPrefix, pklFileName).toString();
        Body<String> executeResult = executePythonScript(fullCmd, pklFilePath);

        if (executeResult.getCode() == 1) {
            return Body.success(pklFilePath, "Embedding生成成功");
        } else {
            return executeResult;
        }
    }

    /**
     * @param filterDTO 筛选条件
     * @param outputPrefix 输出文件前缀（新增参数）
     * @return .pkl文件路径
     */
    public Body<String> query2Embeddings(VerdictFilterDTO filterDTO, String outputPrefix) {
        try {
            logger.info("开始执行一站式Embedding生成服务...");

            // 1. 第一步：根据筛选条件获取文件ID列表（逻辑不变）
            logger.info("步骤 1/2: 根据筛选条件获取文件ID...");
            Body<List<String>> fileIdsBody = this.getDestIds(filterDTO);
            if (fileIdsBody.getCode() != 1 || fileIdsBody.getData() == null || fileIdsBody.getData().isEmpty()) {
                String errorMsg = "未能获取有效文件ID，无法生成Embedding。原因: " + fileIdsBody.getMessage();
                logger.error(errorMsg);
                return Body.error(errorMsg);
            }
            List<String> fileIds = fileIdsBody.getData();
            logger.info("成功获取 {} 个文件ID。", fileIds.size());

            // 2. 第二步：传递 outputPrefix 参数给 getDataEmbeddings
            logger.info("步骤 2/2: 根据文件ID生成Embedding...");
            Body<String> embeddingBody = this.getDataEmbeddings(fileIds, outputPrefix);
            if (embeddingBody.getCode() != 1) {
                logger.error("生成Embedding失败: {}", embeddingBody.getMessage());
                return embeddingBody;
            }

            // 3. 补充校验（逻辑不变）
            String pklFilePath = embeddingBody.getData();
            java.io.File pklFile = new java.io.File(pklFilePath);
            if (!pklFile.exists() || !pklFile.isFile()) {
                String errorMsg = "生成的pkl文件不存在或不是有效文件：" + pklFilePath;
                logger.error(errorMsg);
                return Body.error(errorMsg);
            }

            logger.info("一站式Embedding生成服务成功完成。");
            return embeddingBody;

        } catch (Exception e) {
            logger.error("一站式Embedding生成服务发生未知异常", e);
            return Body.error("系统内部错误：" + e.getMessage());
        }
    }

    private Body<String> executePythonScript(String fullCmd, String expectedOutputFilePath) {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd.exe", "/c", fullCmd);
            } else {
                processBuilder.command("/bin/bash", "-c", fullCmd);
            }
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            readProcessOutput(process);

            boolean isCompleted = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!isCompleted) {
                process.destroyForcibly();
                return Body.error("脚本执行超时，已强制终止");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return Body.error("脚本执行失败，退出码：" + exitCode);
            }

            if (!Files.exists(Paths.get(expectedOutputFilePath))) {
                return Body.error("脚本执行成功，但未找到生成的文件：" + expectedOutputFilePath);
            }

            return Body.success(expectedOutputFilePath, "脚本执行成功");

        } catch (IOException e) {
            logger.error("执行脚本时发生IO异常", e);
            return Body.error("执行脚本失败：" + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("脚本执行被中断", e);
            Thread.currentThread().interrupt();
            return Body.error("脚本执行被中断：" + e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * 获取系统可用的Python命令（优先python3，其次python）
     * @return 可用的Python命令字符串（如"python3"或"python"）
     */
    private String getPythonCommand() {
        // 测试python是否可用
        if (isCommandAvailable("python")) {
            return "python";
        }
        // 测试python3是否可用
        if (isCommandAvailable("python3")) {
            return "python3";
        }
        // 两者都不可用：抛出异常（需安装Python环境）
        throw new RuntimeException("系统未找到Python环境（python/python3命令均不可用），请先安装Python并配置环境变量");
    }

    /**
     * 检查系统命令是否可用（如python3、python）
     * @param command 待检查的命令
     * @return true=可用，false=不可用
     */
    private boolean isCommandAvailable(String command) {
        Process process = null;
        try {
            // 执行命令：command --version（仅测试是否能启动，不关心输出）
            process = new ProcessBuilder(command, "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS); // 5秒内返回视为可用
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 读取进程输出（stdout/stderr）并记录日志
     * @param process 执行中的进程
     */
    private void readProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("Python脚本输出：{}", line);
                }
            } catch (IOException e) {
                logger.error("读取Python脚本输出时发生异常", e);
            }
        }).start();
    }
}
