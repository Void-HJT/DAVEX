package DavexCenter.module.verdict;

import DavexCenter.entity.VerdictTask;
import DavexCenter.mapper.VerdictTaskMapper;
import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.info.VerdictFilterDTO;
import DavexBase.service.auth.CenterWebClientService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VerdictService {
    private static final Logger logger = LoggerFactory.getLogger(VerdictService.class);
    private static final String PKL_SAVE_DIR = "output";
//    private static final String PKL_FILE_NAME_PREFIX = "center_vectorizer_";
    // 简化后的状态常量
    private static final String TASK_STATUS_FILTERING = "数据筛选中";
    private static final String TASK_STATUS_COMPLETED = "筛选完成";
    // 固定应用ID
    private static final String FIXED_APPLICATION_ID = "DAVEX-C1-A1";

    private static final long TIMEOUT_MINUTES = 30; // 脚本执行超时时间（防止无限阻塞）
    // 新增：query目录名称（保存上传文件）
    private static final String QUERY_DIR = "query";
    // 新增：Python脚本名称（p0preprocess.py，需确保在my.core_path下）
    private static final String PY_SCRIPT_NAME = "p0preprocess.py";
    // 新增：生成的emb文件名称（固定输出名称，可根据需求调整）
//    private static final String EMB_OUTPUT_FILE_NAME = "query_emb.txt";
    // 新增：文件后缀标识常量（便于维护）
    private static final String PKL_SUFFIX_TAG = "pkl";
    private static final String EMB_SUFFIX_TAG = "queries";
    private static final String FILE_SEPARATOR = "-P0-"; // 命名分隔符

    @Autowired
    private My my;
    @Autowired
    private CenterWebClientService centerWebClientService;
    @Autowired
    private VerdictTaskMapper verdictTaskMapper;

    /**
     * Center端转发查询到Agent端，记录任务状态（仅筛选中/筛选完成）
     * @param agentId 目标AgentID
     * @param filterDTO 筛选条件
     * @return Body<String>：code=1成功，code≠1失败
     */
    public Body<String> sendQuery(String agentId, VerdictFilterDTO filterDTO) {
        AtomicLong taskId = new AtomicLong();
        try {
            logger.info("Center端开始转发查询条件到Agent端，AgentID：{}，筛选条件：{}", agentId, filterDTO);

            if (agentId == null || agentId.trim().isEmpty()) {
                logger.error("Center端发送查询失败：目标AgentId未配置");
                return Body.error("目标Agent未配置，无法发起预处理请求");
            }

            // 1. 创建任务记录（逻辑不变）
            VerdictTask task = new VerdictTask();
            task.setAgentId(agentId);
            task.setApplicationId(FIXED_APPLICATION_ID);
            task.setStartTime(new Timestamp(System.currentTimeMillis()));
            task.setFilterStart(filterDTO.getJudgeTimeStart());
            task.setFilterEnd(filterDTO.getJudgeTimeEnd());
            task.setFilterType(filterDTO.getJudgeType());
            task.setFilterDistrict(filterDTO.getJudgeDistrict());
            task.setFilterCause(filterDTO.getJudgeCause());
            task.setStatus(TASK_STATUS_FILTERING);
            verdictTaskMapper.insert(task);
            taskId.set(task.getUid());
            logger.info("Center端创建任务记录成功，任务ID：{}，AgentID：{}", taskId.get(), agentId);

            // 2. 构建outputPrefix（核心：任务id_时间戳，作为传递给Agent的前缀）
            String timeSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String outputPrefix = taskId.get() + "_" + timeSuffix; // 对应Agent接口的outputPrefix参数
            logger.info("构建输出前缀outputPrefix：{}，将传递给Agent端", outputPrefix);

            // 3. 调用Agent端接口（逻辑不变）
            WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
            if (webClient == null) {
                logger.error("Center端发送查询失败：创建Agent通信客户端失败，agentId={}", agentId);
                updateTaskStatus(taskId.get(), TASK_STATUS_COMPLETED);
                return Body.error("创建Agent通信连接失败，请检查Agent状态");
            }

            WebClient.ResponseSpec responseSpec = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/verdict/query2Embeddings")
                            .queryParam("outputPrefix", outputPrefix) // 传递outputPrefix参数
                            .build())
                    .bodyValue(filterDTO)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve();

            // 4. 解析响应并处理文件（修改：创建任务专属文件夹+调整pkl保存路径）
            Mono<Body<String>> resultMono = responseSpec.toEntity(Resource.class)
                    .map(responseEntity -> {
                        HttpHeaders headers = responseEntity.getHeaders();
                        Resource fileResource = responseEntity.getBody();

                        String codeStr = headers.getFirst("X-Code");
                        String message = headers.getFirst("X-Message");
                        if (message != null) {
                            message = decodeURIComponent(message);
                        }
                        int code = (codeStr != null && !codeStr.isEmpty()) ? Integer.parseInt(codeStr) : 0;

                        updateTaskStatus(taskId.get(), TASK_STATUS_COMPLETED);

                        if (code != 1 || fileResource == null || !fileResource.exists()) {
                            String errorMsg = message != null ? message : "Agent端预处理失败，未返回有效文件";
                            logger.error("Center端接收Agent文件失败：{}，任务ID：{}", errorMsg, taskId.get());
                            return Body.error("Agent端预处理失败：" + errorMsg);
                        }

                        String corePath = my.getCore_path();
                        if (corePath == null || corePath.trim().isEmpty()) {
                            throw new RuntimeException("Center端核心路径未配置，无法保存文件");
                        }
                        // 关键修改1：构建output根目录和任务专属文件夹路径
                        File outputRootDir = new File(corePath, PKL_SAVE_DIR);
                        File taskFolder = new File(outputRootDir, outputPrefix); // 文件夹名=outputPrefix

                        // 关键修改2：创建任务专属文件夹（若不存在）
                        if (!outputRootDir.exists()) {
                            outputRootDir.mkdirs();
                        }
                        if (!taskFolder.exists()) {
                            boolean mkdirSuccess = taskFolder.mkdirs();
                            if (!mkdirSuccess) {
                                String errorMsg = "创建任务专属文件夹失败，路径：" + taskFolder.getAbsolutePath();
                                logger.error(errorMsg);
                                throw new RuntimeException(errorMsg);
                            }
                            logger.info("成功创建Center端任务专属文件夹：{}", taskFolder.getAbsolutePath());
                        }
                        // 校验：确保任务路径是文件夹（防止被文件占用）
                        if (taskFolder.exists() && !taskFolder.isDirectory()) {
                            String errorMsg = "任务专属文件夹路径被文件占用，路径：" + taskFolder.getAbsolutePath();
                            logger.error(errorMsg);
                            throw new RuntimeException(errorMsg);
                        }

                        // 关键修改3：pkl文件保存到任务专属文件夹，文件名不变
                        String newFileName = outputPrefix + FILE_SEPARATOR + PKL_SUFFIX_TAG; // 文件名保持原有规则不变
                        File localFile = new File(taskFolder, newFileName); // 路径改为任务专属文件夹

                        try (OutputStream outputStream = new FileOutputStream(localFile)) {
                            FileCopyUtils.copy(fileResource.getInputStream(), outputStream);
                        } catch (IOException e) {
                            String errorMsg = "文件保存失败：" + localFile.getAbsolutePath() + "，原因：" + e.getMessage();
                            logger.error(errorMsg, e);
                            throw new RuntimeException(errorMsg, e);
                        }

                        String localFilePath = localFile.getAbsolutePath();
                        String resultData = localFilePath + "|" + outputPrefix + "|" + taskId.get();
                        logger.info("Center端成功保存文件到本地：{}，基础文件名：{}，任务ID：{}", localFilePath, outputPrefix, taskId.get());
                        return Body.success(resultData, "Agent端预处理成功，文件已保存到Center：" + localFilePath);
                    });

            return resultMono.block();

        } catch (Exception e) {
            String errorMsg = "跨Center-Agent通信异常：" + e.getMessage();
            logger.error("Center端转发查询异常，AgentID：{}，任务ID：{}", agentId, taskId.get(), e);
            if (taskId.get() > 0) {
                updateTaskStatus(taskId.get(), TASK_STATUS_COMPLETED);
            }
            return Body.error(errorMsg);
        }
    }

    /**
     * 最简版：仅更新任务状态为指定值
     */
    private void updateTaskStatus(Long taskId, String status) {
        if (taskId == null || taskId <= 0) {
            logger.warn("更新任务状态失败：任务ID为空或无效");
            return;
        }
        try {
            LambdaUpdateWrapper<VerdictTask> updateWrapper = new LambdaUpdateWrapper<VerdictTask>()
                    .eq(VerdictTask::getUid, taskId)
                    .set(VerdictTask::getStatus, status);
            verdictTaskMapper.update(null, updateWrapper);
            logger.info("任务状态更新完成，任务ID：{}，状态：{}", taskId, status);
        } catch (Exception e) {
            logger.error("更新任务状态异常，任务ID：{}", taskId, e);
        }
    }

    // 原updateTaskStatus方法保留，新增重载方法：更新状态+备注
    private void updateTaskStatusAndRemark(Long taskId, String status, String remark) {
        if (taskId == null || taskId <= 0) {
            logger.warn("更新任务状态和备注失败：任务ID为空或无效");
            return;
        }
        try {
            LambdaUpdateWrapper<VerdictTask> updateWrapper = new LambdaUpdateWrapper<VerdictTask>()
                    .eq(VerdictTask::getUid, taskId)
                    .set(VerdictTask::getStatus, status)
                    .set(VerdictTask::getRemark, remark); // 新增：更新remark字段 // 可选：新增结束时间（若VerdictTask有该字段）
            verdictTaskMapper.update(null, updateWrapper);
            logger.info("任务状态和备注更新完成，任务ID：{}，状态：{}，备注：{}", taskId, status, remark);
        } catch (Exception e) {
            logger.error("更新任务状态和备注异常，任务ID：{}", taskId, e);
        }
    }

    /**
     * 解码URL编码字符串
     */
    private String decodeURIComponent(String encodedStr) {
        try {
            return java.net.URLDecoder.decode(encodedStr, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.warn("解码字符串失败：{}", encodedStr, e);
            return encodedStr;
        }
    }

    /**
     * 上传输入文件并执行Python脚本生成emb文件
     * @param inputFile 上传的输入文本文件
     * @param pklPath pkl文件的绝对路径
     * @return Body<String>：code=1成功（data=emb文件绝对路径），code≠1失败
     */
    public Body<String> computeInput(MultipartFile inputFile, String pklPath, String baseFileName, Long taskId) {
        try {
            logger.info("开始执行computeInput：上传文件名称={}，pkl文件路径={}", inputFile.getOriginalFilename(), pklPath);

            // 原有参数校验逻辑不变...
            if (inputFile == null || inputFile.isEmpty()) {
                logger.error("computeInput失败：上传的输入文件为空");
                updateTaskStatusAndRemark(taskId, "输入处理失败", "上传的输入文件为空");
                return Body.error("输入文件不能为空，请重新上传");
            }
            if (pklPath == null || pklPath.trim().isEmpty() || !Files.exists(Paths.get(pklPath))) {
                logger.error("computeInput失败：pkl文件路径无效或文件不存在，pklPath={}", pklPath);
                updateTaskStatusAndRemark(taskId, "输入处理失败", "pkl文件路径无效或文件不存在：" + pklPath);
                return Body.error("pkl文件路径无效或文件不存在，请检查");
            }
            String corePath = my.getCore_path();
            if (corePath == null || corePath.trim().isEmpty()) {
                logger.error("computeInput失败：Center端核心路径未配置");
                return Body.error("系统核心路径未配置，无法执行脚本");
            }

            updateTaskStatusAndRemark(taskId, "输入处理中", "开始处理上传文件：" + inputFile.getOriginalFilename() + "，pkl文件路径：" + pklPath);
            logger.info("任务ID：{}，状态更新为“输入处理中”", taskId);

            // 原有query目录创建、上传文件保存逻辑不变...
            File queryDir = new File(corePath, QUERY_DIR);
            if (!queryDir.exists()) {
                boolean mkdirSuccess = queryDir.mkdirs();
                if (!mkdirSuccess) {
                    String errorMsg = "创建query目录失败，路径：" + queryDir.getAbsolutePath();
                    logger.error(errorMsg);
                    updateTaskStatusAndRemark(taskId, "输入处理失败", errorMsg);
                    return Body.error(errorMsg);
                }
            }
            if (queryDir.exists() && !queryDir.isDirectory()) {
                String errorMsg = "query目录路径被文件占用，路径：" + queryDir.getAbsolutePath();
                logger.error(errorMsg);
                return Body.error(errorMsg);
            }

            String originalFileName = inputFile.getOriginalFilename();
            File savedInputFile = new File(queryDir, originalFileName);
            try (OutputStream outputStream = new FileOutputStream(savedInputFile)) {
                FileCopyUtils.copy(inputFile.getInputStream(), outputStream);
            } catch (IOException e) {
                String errorMsg = "保存上传文件失败：" + savedInputFile.getAbsolutePath() + "，原因：" + e.getMessage();
                logger.error(errorMsg, e);
                return Body.error(errorMsg);
            }
            logger.info("上传文件已成功保存到：{}", savedInputFile.getAbsolutePath());

            // 原有Python脚本路径校验逻辑不变...
            File pyScriptFile = new File(corePath, PY_SCRIPT_NAME);
            if (!pyScriptFile.exists() || !pyScriptFile.isFile()) {
                String errorMsg = "Python脚本不存在，路径：" + pyScriptFile.getAbsolutePath();
                logger.error(errorMsg);
                updateTaskStatusAndRemark(taskId, "输入处理失败", errorMsg);
                return Body.error(errorMsg);
            }

            // 关键修改1：构建output根目录和任务专属文件夹路径（文件夹名=baseFileName）
            File outputRootDir = new File(corePath, PKL_SAVE_DIR);
            File taskFolder = new File(outputRootDir, baseFileName); // 文件夹名=baseFileName（即outputPrefix）

            // 关键修改2：确保任务专属文件夹存在（无需重复创建，兼容sendQuery已创建的情况）
            if (!outputRootDir.exists()) {
                outputRootDir.mkdirs();
            }
            if (!taskFolder.exists()) {
                boolean mkdirSuccess = taskFolder.mkdirs();
                if (!mkdirSuccess) {
                    String errorMsg = "创建任务专属文件夹失败，路径：" + taskFolder.getAbsolutePath();
                    logger.error(errorMsg);
                    updateTaskStatusAndRemark(taskId, "输入处理失败", errorMsg);
                    return Body.error(errorMsg);
                }
                logger.info("成功创建Center端任务专属文件夹：{}", taskFolder.getAbsolutePath());
            }
            // 校验：确保任务路径是文件夹
            if (taskFolder.exists() && !taskFolder.isDirectory()) {
                String errorMsg = "任务专属文件夹路径被文件占用，路径：" + taskFolder.getAbsolutePath();
                logger.error(errorMsg);
                updateTaskStatusAndRemark(taskId, "输入处理失败", errorMsg);
                return Body.error(errorMsg);
            }

            // 关键修改3：emb文件保存到任务专属文件夹，文件名不变
            String embFileName = baseFileName + FILE_SEPARATOR + EMB_SUFFIX_TAG; // 文件名保持原有规则不变
            File embOutputFile = new File(taskFolder, embFileName); // 路径改为任务专属文件夹
            String embOutputFilePath = embOutputFile.getAbsolutePath();

            // 原有Python命令拼接、脚本执行逻辑不变...
            String pythonPath = "/disk/zkx/miniconda3/bin/python";
            String fullCmd = String.format(
                    "%s \"%s\" \"%s\" \"%s\" -o \"%s\"",
                    pythonPath,
                    pyScriptFile.getAbsolutePath(),
                    savedInputFile.getAbsolutePath(),
                    pklPath,
                    embOutputFilePath
            );
            logger.info("拼接后的Python执行命令：{}", fullCmd);

            Body<String> scriptResult = executePythonScript(fullCmd, embOutputFilePath);
            if (scriptResult.getCode() != 1) {
                logger.error("Python脚本执行失败，错误信息：{}", scriptResult.getMessage());
                updateTaskStatusAndRemark(taskId, "输入处理失败", scriptResult.getMessage());
                return Body.error("脚本执行失败：" + scriptResult.getMessage());
            }

            logger.info("computeInput执行成功，生成的emb文件路径：{}", embOutputFilePath);
            updateTaskStatusAndRemark(taskId, "输入处理完成", "上传文件处理成功，emb文件路径：" + embOutputFilePath + "，脚本执行成功");
            logger.info("任务ID：{}，状态更新为“输入处理完成”", taskId);
            return Body.success(embOutputFilePath, "emb文件生成成功，路径：" + embOutputFilePath);

        } catch (Exception e) {
            String errorMsg = "computeInput执行异常：" + e.getMessage();
            logger.error(errorMsg, e);
            updateTaskStatusAndRemark(taskId, "输入处理失败", "上传文件处理失败，原因：" + errorMsg);
            logger.info("任务ID：{}，状态更新为“输入处理失败”", taskId);
            return Body.error(errorMsg);
        }
    }

    /**
     * 串联流程：先发送查询获取pkl文件，再上传输入文件执行脚本生成emb文件
     * @param agentId 目标AgentID
     * @param filterDTO 筛选条件
     * @param inputFile 上传的输入文本文件
     * @return Body<String>：code=1成功（data=emb文件绝对路径），code≠1失败
     */
    public Body<String> sendAndCompute(String agentId, VerdictFilterDTO filterDTO, MultipartFile inputFile) {
        try {
            logger.info("开始执行sendAndCompute串联流程，AgentID：{}，上传文件名称：{}", agentId, inputFile.getOriginalFilename());

            // 1. 第一步：调用sendQuery获取pkl文件本地路径
            Body<String> sendQueryResult = sendQuery(agentId, filterDTO);
            if (sendQueryResult.getCode() != 1) {
                String errorMsg = "sendQuery执行失败，无法继续生成emb文件：" + sendQueryResult.getMessage();
                logger.error(errorMsg);
                return Body.error(errorMsg);
            }
//            String pklFilePath = sendQueryResult.getData();
//            logger.info("sendQuery执行成功，获取到pkl文件路径：{}", pklFilePath);
            // 新增：拆分pkl路径和基础文件名
            String[] resultArr = sendQueryResult.getData().split("\\|");
            if (resultArr.length != 3) { // 原2个元素改为3个
                logger.error("sendQuery返回数据格式异常，无法解析pkl路径、基础文件名和任务ID");
                return Body.error("获取pkl文件信息失败，无法生成emb文件");
            }
            String pklFilePath = resultArr[0];
            String baseFileName = resultArr[1];
            Long taskId = Long.parseLong(resultArr[2]); // 新增：获取任务ID
            logger.info("sendQuery执行成功，获取到pkl文件路径：{}，基础文件名：{}，任务ID：{}", pklFilePath, baseFileName, taskId);

            // 2. 第二步：调用computeInput生成emb文件
//            Body<String> computeInputResult = computeInput(inputFile, pklFilePath);
            Body<String> computeInputResult = computeInput(inputFile, pklFilePath, baseFileName, taskId);
            if (computeInputResult.getCode() != 1) {
                String errorMsg = "computeInput执行失败：" + computeInputResult.getMessage();
                logger.error(errorMsg);
                return Body.error(errorMsg);
            }
            String embFilePath = computeInputResult.getData();
            logger.info("sendAndCompute串联流程执行成功，最终生成emb文件路径：{}", embFilePath);

            // 3. 返回最终结果
            return Body.success(embFilePath, "串联流程执行成功，emb文件已生成：" + embFilePath);

        } catch (Exception e) {
            String errorMsg = "sendAndCompute串联流程执行异常：" + e.getMessage();
            logger.error(errorMsg, e);
            return Body.error(errorMsg);
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