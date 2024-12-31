package DavexCenter.module.task.service;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.entity.Agent;
import DavexBase.entity.FlTask;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.FlTaskMapper;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.file.controller.FlFileController;
import DavexCenter.module.file.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DavexBase.service.auth.CenterWebClientService;
import DavexBase.mapper.AgentMapper;
import DavexBase.entity.Agent;
import DavexBase.entity.FlTask;
import DavexBase.mapper.FlTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
@Service
public class SecretFlowService {
    private static final Logger logger = LoggerFactory.getLogger(SecretFlowService.class);
    @Autowired
    private FileService fileService;
    @Autowired
    private FlFileController flFileController;
    @Autowired
    private My my;
    @Autowired
    private CenterWebClientService centerWebClientService;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private FlTaskMapper flTaskMapper;




    // 方法接受一个字符串参数作为命令，并执行它

    @Async
    public void runFlTask(String taskName,String outputPath){
        String mainC = String.format("source /home/zw/DAVEX/sfenv/bin/activate && python %s/%s.py --result_dir %s/result/%s",my.getEnv_path(),taskName,my.getBase_path(),outputPath);
        List<String> command = new ArrayList<>(Arrays.asList(
                "bash", "-c", mainC));

        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(new java.io.File(my.getEnv_path()));
        try {
            Process process = processBuilder.start();
            try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = stdOut.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
                logger.info("Command output: {}", output);

                StringBuilder errorMsg = new StringBuilder();
                while ((line = stdErr.readLine()) != null) {
                    errorMsg.append(line).append(System.lineSeparator());
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    logger.error("Command error: {}", errorMsg);
                    throw new RuntimeException(errorMsg.toString());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            logger.error("Thread was interrupted", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("An error occurred while running the task", e);
            throw new RuntimeException(e);
        }
        String result = "accuracy.png";
        saveFile(result);
    }

    @Async
    public Body<String> executeAsyncCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            command = "source /home/zw/DAVEX/sfenv/bin/activate &&"+command;
            processBuilder.command("bash", "-c", command);
            processBuilder.directory(new java.io.File(my.getEnv_path()));
            // 启动进程并获取输出
            Process process = processBuilder.start();

            // 捕获标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 捕获错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return Body.success(output.toString() + my.getEnv_path());
            } else {
                return Body.error("Command execution failed with exit code: " + exitCode + "\nError Output: "
                        + errorOutput.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
            return Body.error("The process was interrupted");
        }
    }

    public Body<String> executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            command = "source /home/zw/DAVEX/sfenv/bin/activate &&"+command;
            processBuilder.command("/bin/bash", "-c", command);
            processBuilder.directory(new java.io.File(my.getEnv_path()));
            processBuilder.environment().forEach((key, value) -> logger.info(key + "=" + value));
            logger.info("运行命令目录：" + processBuilder.directory().getAbsolutePath());
            logger.info("运行命令内容：" + String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();

            // 捕获标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 捕获错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return Body.success(output.toString() + my.getEnv_path());
            } else {
                return Body.error("Command execution failed with exit code: " + exitCode + "\nError Output: "
                        + errorOutput.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
            return Body.error("The process was interrupted");
        }
    }

    public Body<String> saveFile(MultipartFile file) {
        String hash = fileService.getSha256(file);
        String applicationId = "Davex-C1-A1";
        return flFileController.saveFl(file, hash, applicationId, null);
    }

    public void saveFile (String fileName){
        String filePath = "/home/zw/SFFL/sLresult/" + fileName;
        File file = new File(filePath);

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MultipartFile multipartFile = new MockMultipartFile(
                    file.getName(),
                    file.getName(),
                    null,
                    fileInputStream);
            String hash = fileService.getSha256(multipartFile);
            String applicationId = "Davex-C1-A1";
            flFileController.saveFl(multipartFile, hash, applicationId, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Body<String> getRayStatusFromAgent(String agentId) {
        try {
            return centerWebClientService.center2AgentWebClient(agentId).get()
                    .uri(uriBuilder -> uriBuilder.path("/SecretFlowTask/getRayStatus")
                            .build())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Body<String>>() {
                    })
                    .block();
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

    public Body<String> chooseAgentJionRay(String agentId, String ip, String port, String name) {
        try {
            Body<String> response = centerWebClientService.center2AgentWebClient(agentId).get()
                    .uri(uriBuilder -> uriBuilder.path("/SecretFlowTask/joinRay")
                            .queryParam("ip", ip)
                            .queryParam("port", port)
                            .queryParam("name", name)
                            .build())
                    .retrieve().bodyToMono(new ParameterizedTypeReference<Body<String>>() {
                    })
                    .block();
            return response;
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

    public Body<FlTask> createFlTask(FlTask flTask) {
        try {
            int result = flTaskMapper.insert(flTask);
            return Body.success(flTask, "成功");
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

    // 调用网络接口 配置 url
    // 在数据库中插入一条agent信息
    public Body<String> addAgent(String uid, String name, String ip, Integer port, String description) {
        try {
            Agent agent = new Agent();
            agent.setUid(uid);
            agent.setName(name);
            agent.setIp(ip);
            agent.setPort(port);
            agent.setDescription(description);
            int result = agentMapper.insert(agent);
            return Body.success("成功");
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

}
