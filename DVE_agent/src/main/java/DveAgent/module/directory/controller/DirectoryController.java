package DveAgent.module.directory.controller;

import java.util.List;

import DveAgent.module.directory.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import DveBase.common.Body;
import DveBase.entity.Agent;
import DveBase.entity.Application;
import DveBase.entity.File;
import DveBase.entity.Group;
import DveBase.entity.Rule;
import DveBase.info.DirectoryInfo;
import DveBase.info.FileInfo;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory")

public class DirectoryController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DirectoryService directoryService;

    private static final String BASE_DIRECTORY = "/Users/dengruotao/Desktop/result";
//    private static final String BASE_DIRECTORY = "/disk2/DVE/result";

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

//    @PostMapping("/getApplicationAndGroup")
//    public Body<List<ApplicationInfo>> getApplicationAndGroup(@RequestParam("agentId") Long agentId,
//                                                              @RequestParam("centerId") Long centerId){
//        return directoryService.getApplicationAndGroup(agentId,centerId);
//
//    }


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

    @PostMapping("/getRuleByGroup")
    public Body<List<Rule>> getRuleByGroup(@RequestParam("agentId") Long agentId,
                                                 @RequestParam("groupId") Long groupId){
        return directoryService.getRuleByGroup(agentId,groupId);
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


//    @PostMapping("/showFile")
//    public  Body<File> showFile(@RequestParam("uid") Long uid,
//                                @RequestParam("agentId") Long agentId,
//                                @RequestParam("folderId") Long folderId) {
//
//        return directoryService.showFile(uid,agentId,folderId);
//    }


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

        return directoryService.getFilePath(fileId,agentId,folderId,BASE_DIRECTORY);
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

    @PostMapping("/newAgent")
    public ResponseEntity<String> newAgent(@RequestParam("agentId") Long agentId,
                                          @RequestParam("name") String name) {


        Agent agent = new Agent();
        agent.setName(name);agent.setUid(agentId);
        directoryService.addAgent(agent);
        // 发送到Center进行同步
        String centerUrl = "http://10.176.37.50:8080/directory/sync";
        try {
            restTemplate.postForObject(centerUrl, agent, String.class);
        } catch (Exception e) {
            // 处理发送失败的情况，如重试、记录日志等
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to sync file with center");
        }
        return ResponseEntity.ok("File saved and sent to center");
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncFile(@RequestBody Agent agent) {
        // 处理文件数据并保存到数据库
        directoryService.addAgent(agent);
        return ResponseEntity.ok("File synchronized successfully");
    }







}
