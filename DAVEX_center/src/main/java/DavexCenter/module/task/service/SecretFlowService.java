package DavexCenter.module.task.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
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
import DavexBase.mapper.AgentMapper;
import DavexBase.entity.Agent;
import DavexBase.entity.FlTask;
import DavexBase.mapper.FlTaskMapper;

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
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private FlTaskMapper flTaskMapper;




    // 方法接受一个字符串参数作为命令，并执行它
    @Async
    public Body<String> executeAsyncCommand(String command) {
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



    public Body<String> getRayStatusFromAgent(String agentId) {
        try {
            return centerWebClientService.center2AgentWebClient(agentId).get()
                    .uri(uriBuilder -> uriBuilder.path("/SecretFlowTask/getRayStatus")
                            .build()).retrieve().bodyToMono(new ParameterizedTypeReference<Body<String>>() {
                    })
                    .block();
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

    public Body<String> chooseAgentJionRay(String agentId,String ip,String port,String name){
        try {
            Body<String> response = centerWebClientService.center2AgentWebClient(agentId).get()
                    .uri(uriBuilder -> uriBuilder.path("/SecretFlowTask/joinRay")
                            .queryParam("ip",ip)
                            .queryParam("port",port)
                            .queryParam("name",name)
                            .build()).retrieve().bodyToMono(new ParameterizedTypeReference<Body<String>>() {
                    })
                    .block();
            return response;
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }
    public Body<FlTask> createFlTask(FlTask flTask){
        try {
            int result = flTaskMapper.insert(flTask);
            return Body.success(flTask,"成功");
        }catch (Exception e){
            return Body.error(e.getMessage());
        }
    }

    //调用网络接口 配置 url
    //在数据库中插入一条agent信息
    public Body<String> addAgent(String uid,String name,String ip,Integer port,String description){
        try{
            Agent agent = new Agent();
agent.setUid(uid);
agent.setName(name);
agent.setIp(ip);
agent.setPort(port);
agent.setDescription(description);
int result = agentMapper.insert(agent);
return Body.success("成功");
        }catch(Exception e){
            return Body.error(e.getMessage());
        }
    }


}

