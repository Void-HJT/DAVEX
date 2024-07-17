package DveAgent.module.directory;


import DveAgent.common.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController//@RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory")

public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;
    private static final String BASE_DIRECTORY = "/Users/dengruotao/Desktop/result";

    @PostMapping("/createFolder")
    public Body<String> createFolder(@RequestParam("name") String name,
                             @RequestParam("path") String path,
                             @RequestParam("agentId") Integer agentId,
                             @RequestParam("parentId") Integer parentId){
        path = BASE_DIRECTORY+path;
        return directoryService.createFolder(name,path,agentId,parentId);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("fileLocation") String fileLocation,
                             @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }

        try {
            // 获取文件名并构建目标文件路径
            String fileName = file.getOriginalFilename();
            Path targetLocation = Paths.get(BASE_DIRECTORY, fileLocation).toAbsolutePath().normalize().resolve(fileName);

            // 创建目标目录，如果不存在则创建
            Files.createDirectories(targetLocation.getParent());

            // 保存文件到目标路径
            Files.copy(file.getInputStream(), targetLocation);

            return "File uploaded successfully: " + targetLocation.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Could not upload the file: " + ex.getMessage();
        }
    }

}
