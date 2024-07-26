package DveAgent.module.directory;

import DveAgent.common.Body;
import DveAgent.entity.Application;
import DveAgent.entity.File;
import DveAgent.entity.Group;
import DveAgent.info.ApplicationInfo;
import DveAgent.info.DirectoryInfo;
import DveAgent.info.FileInfo;
import org.apache.ibatis.annotations.Param;
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

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory")

public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;
//    private static final String BASE_DIRECTORY = "/Users/dengruotao/Desktop/result";
    private static final String BASE_DIRECTORY = "/disk2/DVE/result";

    @PostMapping("/getApplication")
    public Body<List<Application>> getApplication(){
        return directoryService.getApplication();

    }

    @PostMapping("/getGroup")
    public Body<List<Group>> getGroup(@RequestParam("agentId") Long agentId,
                                 @RequestParam("centerId") Long centerId){
        return directoryService.getGroup(agentId,centerId);
    }

    @PostMapping("/addGroup")
    public Body<String> addGroup(@RequestParam("agentId") Long agentId,
                            @RequestParam("centerId") Long centerId,
                            @RequestParam("name") String name){
        return directoryService.addGroup(agentId,centerId,name);
    }

    @PostMapping("/getGroupByApplicationId")
    public Body<List<Group>> getGroupByApplicationId(@RequestParam("agentId") Long agentId,
                                                     @RequestParam("centerId") Long centerId,
                                                     @RequestParam("applicationId") Long applicationId){
        return directoryService.getGroupByApplicationId(agentId,centerId,applicationId);

    }

    @PostMapping("/getApplicationAndGroup")
    public Body<List<ApplicationInfo>> getApplicationAndGroup(@RequestParam("agentId") Long agentId,
                                                              @RequestParam("centerId") Long centerId){
        return directoryService.getApplicationAndGroup(agentId,centerId);

    }

    @PostMapping("/addApplicationGroup")
    public Body<String> addApplicationGroup(@RequestParam("agentId") Long agentId,
                                            @RequestParam("centerId") Long centerId,
                                            @RequestParam("applicationId") Long applicationId,
                                            @RequestParam("groupId") Long groupId){
        return directoryService.addApplicationGroup(agentId,centerId,applicationId,groupId);

    }

    @PostMapping("/addRule")
    public Body<String> addRule(@RequestParam("agentId") Long agentId,
                                @RequestParam("groupId") Long groupId,
                                @RequestParam("allowMethod")String allowMethod){
        return directoryService.addRule(agentId,groupId,allowMethod);

    }



    @PostMapping("/createFolder")
    public Body<String> createFolder(@RequestParam("name") String name,
                @RequestParam("path") String path,
                @RequestParam("agentId") Long agentId,
                @RequestParam("parentId") Long parentId) {
        path = BASE_DIRECTORY + path;
        return directoryService.createFolder(name, path, agentId, parentId);
    }

    @PostMapping("/setFolderVisible")
    public Body<String> setFolderVisible(@RequestParam("agentId") Long agentId,
                                            @RequestParam("groupId") Long groupId,
                                            @RequestParam("folderId") Long folderId){
        return directoryService.setFolderVisible(agentId,groupId,folderId);
    }

    @PostMapping("/setFolderInvisible")
    public Body<String> setFolderInvisible(@RequestParam("agentId") Long agentId,
                                            @RequestParam("groupId") Long groupId,
                                            @RequestParam("folderId") Long folderId){
        return directoryService.setFolderInvisible(agentId,groupId,folderId);
    }

    @PostMapping("/setFolderName")
    public Body<String> setFolderName(@RequestParam("agentId") Long agentId,
                                      @RequestParam("folderId") Long folderId,
                                      @RequestParam("name") String name){

        return directoryService.setFolderName(agentId,folderId,name,BASE_DIRECTORY);
    }

    @PostMapping("/deleteFolder")
    public Body<String> deleteFolder(@RequestParam("agentId") Long agentId,
                                     @RequestParam("folderId") Long folderId){
        return directoryService.deleteFolder(agentId,folderId,BASE_DIRECTORY);
    }

    @PostMapping("/getFolderPath")
    public Body<String> getFolderPath(@RequestParam("agentId") Long agentId,
                                      @RequestParam("folderId") Long folderId){
        return directoryService.getFolderPath(agentId,folderId,BASE_DIRECTORY);
    }


    @PostMapping("/showFile")
    public  Body<File> showFile(@RequestParam("uid") Long uid,
                                @RequestParam("agentId") Long agentId,
                                @RequestParam("folderId") Long folderId) {

        return directoryService.showFile(uid,agentId,folderId);
    }


    @PostMapping("/uploadFile")
    public  Body<String> uploadFile(@RequestParam("agentId") Long agentId,
                                    @RequestParam("folderId") Long folderId,
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
    public Body<FileInfo> getFileInfo(@RequestParam("uid") Long uid,
                                      @RequestParam("agentId") Long agentId,
                                      @RequestParam("folderId") Long folderId){
        return directoryService.getFileInfo(uid,agentId,folderId);
    }

    @PostMapping("/setFileRule")
    public Body<String> setFileRule(@RequestParam("fileId") Long fileId,
                                    @RequestParam("agentId") Long agentId,
                                    @RequestParam("folderId") Long folderId,
                                    @RequestParam("ruleId") Long ruleId){
        return directoryService.setFileRule(fileId,agentId,folderId,ruleId);
    }

    @PostMapping("/deleteFile")
    public Body<String> deleteFile(@RequestParam("fileId") Long fileId,
                                   @RequestParam("agentId") Long agentId,
                                   @RequestParam("folderId") Long folderId){
        return directoryService.deleteFile(fileId,agentId,folderId,BASE_DIRECTORY);
    }

    @PostMapping("/sendFile")
    public ResponseEntity<Resource> sendFile(@RequestParam("fileId") Long fileId,
                                             @RequestParam("agentId") Long agentId,
                                             @RequestParam("folderId") Long folderId){
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


    @PostMapping("/getDirectory")
    public Body<DirectoryInfo> getDirectory(@RequestParam("rootId") Long rootFolderId) {
        DirectoryInfo directory = directoryService.getDirectoryStructure(rootFolderId);
        return Body.success(directory,"1");
    }

    @PostMapping("/getDirectoryByGroup")
    public Body<DirectoryInfo> getDirectory(@RequestParam("rootId") Long rootFolderId,
                                            @RequestParam("agentId") Long agentId,
                                            @RequestParam("groupId") Long groupId) {
        DirectoryInfo directory = directoryService.getDirectoryStructure(rootFolderId);
        directory = directoryService.filterFoldersByVisibility(groupId,agentId,directory);
        directory = directoryService.filterFilesByRule(groupId,agentId,directory);
        return Body.success(directory,"1");
    }







}
