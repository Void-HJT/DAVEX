package DavexCenter.module.task.controller;


import DavexBase.common.Body;
import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexCenter.module.task.service.SecretFlowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
@RestController
@RequestMapping("/SecretFlowTask")
public class SecretFlowController {

    @GetMapping("/execute-script")
    public String executeScript() {
        try {
            // 定义要执行的命令
            String command = "source sfenv/bin/activate && python testFL.py";
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.directory(new java.io.File("/home/zw/SFFL"));

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
                return "Script executed successfully: \n" + output.toString();
            } else {
                return "Script execution failed with exit code: " + exitCode + "\nError Output: " + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

}
