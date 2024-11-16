package DavexAgent.module.task.service;

import DavexBase.common.Body;
import DavexBase.common.My;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Service
public class SecretFlowService {

    @Autowired
    private My my;
    // 方法接受一个字符串参数作为命令，并执行它

    public Body<String> executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.directory(new java.io.File("/home/zkx"));  // 可根据需要更改目录 这个目录 选择配置里的路径吧


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
                return Body.success(output.toString());
            } else {
                return Body.error("Command execution failed with exit code: " + exitCode + "\nError Output: " + errorOutput.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
            return Body.error("The process was interrupted");
        }
    }
//    public String executeCommand(String command) {
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder();
//            processBuilder.command("bash", "-c", command);
//            processBuilder.directory(new java.io.File("/home/zw/SFFL"));  // 可根据需要更改目录
//
//            // 启动进程并获取输出
//            Process process = processBuilder.start();
//
//            // 捕获标准输出
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            StringBuilder output = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            // 捕获错误输出
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            StringBuilder errorOutput = new StringBuilder();
//            while ((line = errorReader.readLine()) != null) {
//                errorOutput.append(line).append("\n");
//            }
//
//            int exitCode = process.waitFor();
//            if (exitCode == 0) {
//                return "Command executed successfully: \n" + output.toString();
//            } else {
//                return "Command execution failed with exit code: " + exitCode + "\nError Output: " + errorOutput.toString();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error occurred: " + e.getMessage();
//        }
//    }


}

