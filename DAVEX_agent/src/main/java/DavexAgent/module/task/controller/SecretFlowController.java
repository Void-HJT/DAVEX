package DavexAgent.module.task.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import DavexAgent.module.task.service.SecretFlowService;
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

    @GetMapping("/joinRay")
    public Body<String> joinRay(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String name
    ) {
        // 构造命令字符串
        String command = String.format("source sfenv/bin/activate && ray start --address=\"%s:%s\"  --resources='{\"%s\": 16}' --disable-usage-stats", ip, port,name);
        // 调用 service 中的方法执行命令
        return secretFlowService.executeCommand(command);
    }

    @GetMapping("/getRayStatus")
    public Body<String> getRayStatus() {
        String command = "source sfenv/bin/activate && ray status";
        try {
            // 执行命令并获取结果
            Body<String> result = secretFlowService.executeCommand(command);
            // 可以根据需要对结果进行处理，比如解析JSON等
            return result;
        } catch (Exception e) {
            // 处理执行命令时发生的任何异常
            // 记录日志、返回错误信息等
            return Body.error("Error executing command: " + e.getMessage());
        }
    }
    //需要优化一下 主节点stop连带着其他人也stop
    @GetMapping("/stop-mainRay")
    public Body<String> stopMainRay() {
        // 调用 service 中的方法执行命令
        return secretFlowService.executeCommand("source sfenv/bin/activate && ray stop");
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
