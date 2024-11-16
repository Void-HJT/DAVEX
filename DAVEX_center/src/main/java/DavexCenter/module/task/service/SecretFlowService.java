package DavexCenter.module.task.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import DavexCenter.module.file.controller.FlFileController;
import DavexCenter.module.file.service.FileService;
import DavexBase.common.My;
import DavexBase.common.Body;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import DavexBase.service.auth.CenterWebClientService;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class SecretFlowService {

    @Autowired
    private FileService fileService;
    @Autowired
    private FlFileController flFileController;
    @Autowired
    private My my;
    @Autowired
    private CenterWebClientService centerWebClientService;




    // 方法接受一个字符串参数作为命令，并执行它
    public Body<String> executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
//            processBuilder.directory(new java.io.File("/home/zw/SFFL"));  // 可根据需要更改目录
            processBuilder.directory(new java.io.File(my.getEnv_path()));
//             启动进程并获取输出
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
                return Body.success(output.toString()+my.getEnv_path());
            } else {
                return Body.error("Command execution failed with exit code: " + exitCode + "\nError Output: " + errorOutput.toString()+my.getGarnet_path());
            }
            } catch (IOException e) {
                e.printStackTrace();
                return Body.error(String.format(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
            return Body.error("The process was interrupted");
        }
    }
    public Body<String> saveFile (MultipartFile file){
        String hash = fileService.getSha256(file);
        String applicationId = "Davex-C1-A1";
        return flFileController.saveFl(file,hash,applicationId,null);
    }

    public Body<String> sendCommandToAgent(String agentId) {
        try {
            Body<String> response = centerWebClientService.center2AgentWebClient(agentId).get()
                    .uri(uriBuilder -> uriBuilder.path("/SecretFlowTask/getRayStatus")
                            .build()).retrieve().bodyToMono(new ParameterizedTypeReference<Body<String>>() {
                    })
                    .block();
            return response;
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

    //调用网络接口 配置 url


}

