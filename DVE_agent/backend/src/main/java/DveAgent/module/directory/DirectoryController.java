package DveAgent.module.directory;


import DveAgent.common.Body;
import DveAgent.entity.File;
import DveAgent.entity.Group;
import DveAgent.info.ApplicationInfo;
import DveAgent.info.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController//@RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory")

public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;
    private static final String BASE_DIRECTORY = "/Users/dengruotao/Desktop/result";

    @PostMapping("/getGroup")
    public Body<List<Group>> getGroup(@RequestParam("agentId") Integer agentId,
                                 @RequestParam("centerId") Integer centerId){
        return directoryService.getGroup(agentId,centerId);
    }

    @PostMapping("/addGroup")
    public Body<String> addGroup(@RequestParam("agentId") Integer agentId,
                            @RequestParam("centerId") Integer centerId,
                            @RequestParam("name") String name){
        return directoryService.addGroup(agentId,centerId,name);
    }

    @PostMapping("/getGroupByApplicationId")
    public Body<List<Group>> getGroupByApplicationId(@RequestParam("agentId") Integer agentId,
                                                     @RequestParam("centerId") Integer centerId,
                                                     @RequestParam("applicationId") Integer applicationId){
        return directoryService.getGroupByApplicationId(agentId,centerId,applicationId);

    }

    @PostMapping("/getApplicationAndGroup")
    public Body<List<ApplicationInfo>> getApplicationAndGroup(@RequestParam("agentId") Integer agentId,
                                                                        @RequestParam("centerId") Integer centerId){
        return directoryService.getApplicationAndGroup(agentId,centerId);

    }

    @PostMapping("/addApplicationGroup")
    public Body<String> addApplicationGroup(@RequestParam("agentId") Integer agentId,
                                            @RequestParam("centerId") Integer centerId,
                                            @RequestParam("applicationId") Integer applicationId,
                                            @RequestParam("groupId") Integer groupId){
        return directoryService.addApplicationGroup(agentId,centerId,applicationId,groupId);

    }

    @PostMapping("/addRule")
    public Body<String> addRule(@RequestParam("agentId") Integer agentId,
                                @RequestParam("groupId") Integer groupId,
                                @RequestParam("allowMethod")String allowMethod){
        return directoryService.addRule(agentId,groupId,allowMethod);

    }



    @PostMapping("/createFolder")
    public Body<String> createFolder(@RequestParam("name") String name,
                             @RequestParam("path") String path,
                             @RequestParam("agentId") Integer agentId,
                             @RequestParam("parentId") Integer parentId){
        path = BASE_DIRECTORY+path;
        return directoryService.createFolder(name,path,agentId,parentId);
    }

    @PostMapping("/setFolderVisible")
    public Body<String> setFolderVisible(@RequestParam("agentId") Integer agentId,
                                            @RequestParam("groupId") Integer groupId,
                                            @RequestParam("folderId") Integer folderId){
        return directoryService.setFolderVisible(agentId,groupId,folderId);
    }

    @PostMapping("/setFolderInvisible")
    public Body<String> setFolderInvisible(@RequestParam("agentId") Integer agentId,
                                            @RequestParam("groupId") Integer groupId,
                                            @RequestParam("folderId") Integer folderId){
        return directoryService.setFolderInvisible(agentId,groupId,folderId);
    }

    @PostMapping("/setFolderName")
    public Body<String> setFolderName(@RequestParam("agentId") Integer agentId,
                                      @RequestParam("folderId") Integer folderId,
                                      @RequestParam("name") String name){

        return directoryService.setFolderName(agentId,folderId,name,BASE_DIRECTORY);
    }

    @PostMapping("/deleteFolder")
    public Body<String> deleteFolder(@RequestParam("agentId") Integer agentId,
                                     @RequestParam("folderId") Integer folderId){
        return directoryService.deleteFolder(agentId,folderId,BASE_DIRECTORY);
    }

    @PostMapping("/getFolderPath")
    public Body<String> getFolderPath(@RequestParam("agentId") Integer agentId,
                                      @RequestParam("folderId") Integer folderId){
        return directoryService.getFolderPath(agentId,folderId,BASE_DIRECTORY);
    }


    @PostMapping("/showFile")
    public  Body<File> showFile(@RequestParam("uid") Integer uid,
                                @RequestParam("agentId") Integer agentId,
                                @RequestParam("folderId") Integer folderId) {

        return directoryService.showFile(uid,agentId,folderId);
    }


    @PostMapping("/uploadFile")
    public  Body<String> uploadFile(@RequestParam("agentId") Integer agentId,
                                    @RequestParam("folderId") Integer folderId,
                                    @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Body.error("上传文件为空");
        }

        return directoryService.uploadFile(agentId,folderId,file,BASE_DIRECTORY);
    }

    @PostMapping("/updateFile")
    public Body<String> updateFile(@RequestBody File file){
        return directoryService.updateFile(file);
    }

    @PostMapping("/getFileInfo")
    public Body<FileInfo> getFileInfo(@RequestParam("uid") Integer uid,
                                      @RequestParam("agentId") Integer agentId,
                                      @RequestParam("folderId") Integer folderId){
        return directoryService.getFileInfo(uid,agentId,folderId);
    }

    @PostMapping("/setFileRule")
    public Body<String> setFileRule(@RequestParam("fileId") Integer fileId,
                                    @RequestParam("agentId") Integer agentId,
                                    @RequestParam("folderId") Integer folderId,
                                    @RequestParam("ruleId") Integer ruleId){
        return directoryService.setFileRule(fileId,agentId,folderId,ruleId);
    }

    @PostMapping("/deleteFile")
    public Body<String> deleteFile(@RequestParam("fileId") Integer fileId,
                                   @RequestParam("agentId") Integer agentId,
                                   @RequestParam("folderId") Integer folderId){
        return directoryService.deleteFile(fileId,agentId,folderId,BASE_DIRECTORY);
    }

    @PostMapping("/sendFile")
    public ResponseEntity<Resource> sendFile(@RequestParam("fileId") Integer fileId,
                                             @RequestParam("agentId") Integer agentId,
                                             @RequestParam("folderId") Integer folderId){
        String filePath = directoryService.getFilePath(fileId,agentId,folderId,BASE_DIRECTORY);
        // 读取文件路径
        Path path = Paths.get(filePath);
        // 文件名从filePath末尾截断获得
        String fileName = path.getFileName().toString();

        // 对文件名进行UTF-8编码以处理中文
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // 发送文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +encodedFileName+"\"")
                .body(resource);
    }





}
