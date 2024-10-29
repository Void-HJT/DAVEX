package DavexCenter.module.task.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import DavexCenter.module.task.service.SecretFlowService;
import DavexBase.common.My;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import DavexBase.common.Body;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile; // 导入 MockMultipartFile 的包
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/SecretFlowTask")
public class SecretFlowController {
    @Autowired
    private SecretFlowService secretFlowService;

    @Autowired
    private My my;
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



    @GetMapping("/active-mainRay")
    public String activateMainRay(
            @RequestParam String port        // 参数化端口
    ) {
        // 构造命令字符串
        String command = String.format("source sfenv/bin/activate && ray start --head --node-ip-address=\"%s\" --port=\"%s\" --resources='{\"alice\": 16}' --include-dashboard=False --disable-usage-stats", my.getIp(), port);

        // 调用 service 中的方法执行命令
        return secretFlowService.executeCommand(command);
    }

    @GetMapping("/joinRay")
    public String joinRay(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String name
    ) {
        // 构造命令字符串
        String command = String.format("source sfenv/bin/activate && ray start --address=\"%s:%s\"  --resources='{\"%s\": 16}' --disable-usage-stats", ip, port,name);

        // 调用 service 中的方法执行命令
        return secretFlowService.executeCommand(command);
    }

    //需要优化一下 主节点stop连带着其他人也stop
    @GetMapping("/stop-mainRay")
    public String stopMainRay() {
        // 调用 service 中的方法执行命令
        return secretFlowService.executeCommand("source sfenv/bin/activate && ray stop");
    }

    @PostMapping("/save/{fileName}")
    public Body<String> saveFile(@PathVariable String fileName) {
        String filePath = "/home/zw/SFFL/result/" + fileName;
        File file = new File(filePath);

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MultipartFile multipartFile = new MockMultipartFile(
                    file.getName(),
                    file.getName(),
                    null,
                    fileInputStream
            );

            // 调用服务保存文件
            return secretFlowService.saveFile(multipartFile);
        } catch (FileNotFoundException e) {
            // 处理文件未找到的异常
            e.printStackTrace();
        } catch (IOException e) {
            // 处理IO异常
            e.printStackTrace();

        }
        return null;
    }
    private static final String UPLOAD_DIR = "/home/zw/SFFL/input/";

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please select a file to upload";
        }

        try {
            // 确保目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 获取文件名
            String fileName = file.getOriginalFilename();

            // 构造文件的保存路径
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            // 将文件内容写入目标文件
            Files.write(filePath, file.getBytes());

            return "成功将 '" + fileName + "' 上传至 " + filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload file: " + e.getMessage();
        }
    }

}
