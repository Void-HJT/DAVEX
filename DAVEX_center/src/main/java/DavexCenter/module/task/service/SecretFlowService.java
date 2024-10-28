package DavexCenter.module.task.service;

import DavexCenter.mapper.OutputMapper;
import DavexCenter.module.file.service.FlFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import DavexCenter.module.file.controller.FlFileController;
import DavexCenter.module.file.service.FileService;
import DavexBase.common.My;
import DavexBase.common.Body;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class SecretFlowService {
    @Autowired
    private FileService fileService;
    @Autowired
    private FlFileController flFileController;
    @Autowired
    private My my;
    // 方法接受一个字符串参数作为命令，并执行它
    public String executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.directory(new java.io.File("/home/zw/SFFL"));  // 可根据需要更改目录

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
                return "Command executed successfully: \n" + output.toString();
            } else {
                return "Command execution failed with exit code: " + exitCode + "\nError Output: " + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }
    public Body<String> saveFile (MultipartFile file){
        String hash = fileService.getSha256(file);
        return flFileController.saveFl(file,hash,my.getId(),null);
    }
}

