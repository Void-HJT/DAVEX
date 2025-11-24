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
    private static final String OUTPUT_PREFIX = "dataset_embeddings"; // 输出文件前缀（对应Python脚本的--prefix参数）
    private static final int SCALE_FACTOR = 10000; // 缩放系数（对应Python脚本的--scale参数）
    private static final long TIMEOUT_MINUTES = 30; // 脚本执行超时时间（防止无限阻塞）

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
            // 1. 构建查询条件（QueryWrapper）
            QueryWrapper<File> queryWrapper = new QueryWrapper<>();

            // 基础条件：judgeTime不为空（必须满足）
            queryWrapper.isNotNull("judge_time"); // 对应数据库字段名（若实体类字段与表字段一致，可直接传属性名）

            // 2. 动态添加可选条件（非空才添加，“且”逻辑）
            if (filterDTO != null) {
                // 2.1 时间段筛选：judgeTime >= start 且 judgeTime <= end
                if (filterDTO.getJudgeTimeStart() != null) {
                    queryWrapper.ge("judge_time", filterDTO.getJudgeTimeStart()); // ge = greater than or equal
                }
                if (filterDTO.getJudgeTimeEnd() != null) {
                    queryWrapper.le("judge_time", filterDTO.getJudgeTimeEnd()); // le = less than or equal
                }

                // 2.2 判决类型：模糊包含（忽略空字符串）
                if (filterDTO.getJudgeType() != null && !filterDTO.getJudgeType().trim().isEmpty()) {
                    queryWrapper.like("judge_type", filterDTO.getJudgeType().trim()); // like = %xxx%
                }

                // 2.3 判决地点：模糊包含
                if (filterDTO.getJudgeDistrict() != null && !filterDTO.getJudgeDistrict().trim().isEmpty()) {
                    queryWrapper.like("judge_district", filterDTO.getJudgeDistrict().trim());
                }

                // 2.4 案由：模糊包含
                if (filterDTO.getJudgeCause() != null && !filterDTO.getJudgeCause().trim().isEmpty()) {
                    queryWrapper.like("judge_cause", filterDTO.getJudgeCause().trim());
                }
            }

            // 3. 执行查询：只查询fileId（即实体类的uid字段），避免查询多余字段
            List<File> fileList = fileMapper.selectList(queryWrapper.select("uid"));

            // 4. 转换为fileId列表（提取uid字段）
            List<String> fileIdList = fileList.stream()
                    .map(File::getUid) // 从File对象中获取uid（即fileId）
                    .collect(Collectors.toList());

            logger.info("筛选判决书文件ID完成：基础条件（judgeTime不为空），可选条件（{}），匹配数量：{}",
                    filterDTO == null ? "无" : filterDTO.toString(), fileIdList.size());

            // 5. 返回结果（即使空列表也返回成功，由前端处理空数据场景）
            return Body.success(fileIdList, "筛选成功，共匹配" + fileIdList.size() + "个文件ID");

        } catch (Exception e) {
            logger.error("筛选判决书文件ID失败", e);
            return Body.error("筛选文件ID异常：" + e.getMessage());
        }
    }

    /**
     * 生成数据集Embeddings并返回vectorizer.pkl文件路径
     * @param fileIds 待处理的文件ID列表
     * @return 成功：Body.data为pkl文件绝对路径；失败：Body包含错误信息
     */
    public Body<String> getDataEmbeddings(List<String> fileIds) {
        // 1. 校验入参（避免空列表）
        if (fileIds == null || fileIds.isEmpty()) {
            logger.error("文件ID列表为空，无法执行Embeddings生成");
            return Body.error("文件ID列表不能为空");
        }

        // 2. 构建Python脚本路径（my.getCore_path() + 脚本名）
        String corePath = my.getCore_path();
        if (corePath == null || corePath.trim().isEmpty()) {
            logger.error("Core路径未配置（my.getCore_path()返回空）");
            return Body.error("系统Core路径未配置，请检查配置项");
        }
        // 处理路径分隔符（兼容Windows/Linux）
        String pyScriptPath = Paths.get(corePath, PY_SCRIPT_NAME).toString();
        // 校验Python脚本是否存在
        if (!Files.exists(Paths.get(pyScriptPath))) {
            logger.error("Python脚本不存在：{}", pyScriptPath);
            return Body.error("数据集处理脚本不存在：" + pyScriptPath);
        }

        // 3. 根据文件ID获取所有文件的绝对路径
        StringJoiner filePathsJoiner = new StringJoiner(" "); // 拼接文件路径（空格分隔，对应Python脚本的input_files参数）
        for (String fileId : fileIds) {
            // 3.1 根据ID查询文件信息（需确保File实体类与数据库表字段匹配）
            File file = fileMapper.selectById(fileId);
            if (file == null) {
                logger.warn("文件ID不存在：{}，已跳过该文件", fileId);
                continue;
            }

            // 3.2 通过FileFolderService获取文件绝对路径
            String filePath = fileFolderService.getFilePath(file, my.getBase_path());
            if (filePath == null || !Files.exists(Paths.get(filePath))) {
                logger.warn("文件路径无效或文件不存在：fileId={}, filePath={}", fileId, filePath);
                continue;
            }

            // 3.3 拼接文件路径（注意：路径含空格时需加引号，此处处理通用场景）
            filePathsJoiner.add("\"" + filePath + "\""); // 加引号避免路径含空格导致命令解析错误
        }

        // 4. 校验有效文件路径（避免所有文件都无效的情况）
        String filePathsStr = filePathsJoiner.toString();
        if (filePathsStr.trim().isEmpty()) {
            logger.error("所有文件ID对应路径无效，无可用文件进行Embeddings生成");
            return Body.error("所有输入文件路径无效，请检查文件ID或文件存储状态");
        }

        // 5. 构造Python脚本执行命令（分操作系统处理Python命令）
        String pythonCmd = getPythonCommand(); // 获取系统可用的Python命令（python/python3）
        String outputDir = Paths.get(corePath, "output").toString(); // 输出目录（放在Core路径下的output文件夹）
        // 拼接完整命令：python 脚本路径 --input_files 路径1 路径2 --prefix 输出前缀 --scale 缩放系数
        String fullCmd = String.format(
                "%s \"%s\" %s -p \"%s\" -s %d",
                pythonCmd,
                pyScriptPath,
                filePathsStr,
                Paths.get(outputDir, OUTPUT_PREFIX).toString(), // 输出前缀带路径（确保输出到指定目录）
                SCALE_FACTOR
        );
        logger.info("开始执行数据集Embeddings生成命令：{}", fullCmd);

        // 6. 执行Python脚本并等待完成
        Process process = null;
        try {
            // 启动进程执行命令
            ProcessBuilder processBuilder = new ProcessBuilder();
            // 分操作系统设置shell（确保命令解析正确）
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd.exe", "/c", fullCmd);
            } else {
                processBuilder.command("/bin/bash", "-c", fullCmd);
            }
            // 重定向错误流（便于捕获脚本执行错误）
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            // 读取脚本输出（日志记录）
            readProcessOutput(process);

            // 等待执行完成（设置超时时间）
            boolean isCompleted = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!isCompleted) {
                // 超时：销毁进程
                process.destroyForcibly();
                logger.error("Python脚本执行超时（超过{}分钟），已强制终止", TIMEOUT_MINUTES);
                return Body.error("数据集处理超时（超过" + TIMEOUT_MINUTES + "分钟），请检查文件大小或脚本效率");
            }

            // 检查执行状态（0=成功，非0=失败）
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("Python脚本执行失败，退出码：{}，命令：{}", exitCode, fullCmd);
                return Body.error("数据集处理脚本执行失败，退出码：" + exitCode + "，请查看日志获取详细错误信息");
            }

            // 7. 构造并返回pkl文件路径（输出目录+前缀+_vectorizer.pkl）
            String pklFilePath = Paths.get(outputDir, OUTPUT_PREFIX + "_vectorizer.pkl").toString();
            if (!Files.exists(Paths.get(pklFilePath))) {
                logger.error("脚本执行成功，但未找到生成的pkl文件：{}", pklFilePath);
                return Body.error("数据集处理成功，但生成的vectorizer.pkl文件不存在");
            }

            logger.info("数据集Embeddings生成完成，pkl文件路径：{}", pklFilePath);
            return Body.success(pklFilePath, "数据集Embeddings生成成功");

        } catch (IOException e) {
            logger.error("执行Python脚本时发生IO异常", e);
            return Body.error("执行数据集处理脚本失败：" + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("脚本执行被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            return Body.error("数据集处理被中断：" + e.getMessage());
        } finally {
            // 确保进程资源释放
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
