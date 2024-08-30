package DavexAgent.module.directory;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.Agent;
import DavexBase.entity.File;
import DavexBase.info.DirectoryInfo;
import DavexBase.info.FileInfo;
import DavexBase.service.directory.FileFolderService;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/directory/fileFolder")
public class FileFolderController {

    @Autowired
    FileFolderService fileFolderService;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private My my;

    @PostMapping("/getFileByRuleOrNot")
    public Body<?> getFileByRuleOrNot(@RequestParam("agentId") Long agentId,
            @RequestParam("applicationId") Long applicationID,
            @RequestParam("fileId") Long fileId,
            @RequestParam("method") String method) {

        return fileFolderService.getFileByRuleOrNot(agentId, applicationID, fileId, method);
    }

    @PostMapping("/createFolder")
    public Body<String> createFolder(@RequestParam("name") String name,
                                     @RequestParam("agentId") Long agentId,
                                     @RequestParam("parentId") Long parentId) {
        String path = my.getBase_path();
        return fileFolderService.createFolder(name, path, agentId, parentId);
    }

    @PostMapping("/setFolderVisible")
    public Body<String> setFolderVisible(@RequestParam("agentId") Long agentId,
            @RequestParam("groupId") Long groupId,
            @RequestParam("folderId") Long folderId) {
        return fileFolderService.setFolderVisible(agentId, groupId, folderId);
    }

    @PostMapping("/setFolderInvisible")
    public Body<String> setFolderInvisible(@RequestParam("agentId") Long agentId,
            @RequestParam("groupId") Long groupId,
            @RequestParam("folderId") Long folderId) {
        return fileFolderService.setFolderInvisible(agentId, groupId, folderId);
    }

    @PostMapping("/setFolderName")
    public Body<String> setFolderName(@RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId,
            @RequestParam("name") String name) {

        return fileFolderService.setFolderName(agentId, folderId, name, my.getBase_path());
    }

    @PostMapping("/deleteFolder")
    public Body<String> deleteFolder(@RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId) {
        return fileFolderService.deleteFolder(agentId, folderId, my.getBase_path());
    }

    @PostMapping("/getFolderPath")
    public Body<String> getFolderPath(@RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId) {
        return fileFolderService.getFolderPath(agentId, folderId, my.getBase_path());
    }

    // 文件

    @PostMapping("/getFileInfo")
    public Body<FileInfo> getFileInfo(@RequestParam("uid") Long uid,
            @RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId) {
        return fileFolderService.getFileInfo(uid, agentId, folderId);
    }

    @PostMapping("/uploadFile")
    public Body<String> uploadFile(@RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId,
            @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Body.error("上传文件为空");
        }

        return fileFolderService.uploadFile(agentId, folderId, file, my.getBase_path());
    }

    @PostMapping("/updateFile")
    public Body<String> updateFile(@RequestBody File file) {
        return fileFolderService.updateFile(file);
    }

    @PostMapping("/setFileRule")
    public Body<String> setFileRule(@RequestParam("fileId") Long fileId,
                                    @RequestParam("agentId") Long agentId,
                                    @RequestParam("folderId") Long folderId,
                                    @RequestParam("groupId") Long groupId,
                                    @RequestParam("allowedMethod") String allowedMethod) {
        return fileFolderService.setFileRule(fileId, agentId, folderId, groupId,allowedMethod);
    }

    @PostMapping("/deleteFileRule")
    public Body<String> deleteFileRule(@RequestParam("fileId") Long fileId,
                                    @RequestParam("agentId") Long agentId,
                                    @RequestParam("folderId") Long folderId,
                                    @RequestParam("groupId") Long groupId,
                                    @RequestParam("allowedMethod") String allowedMethod) {
        return fileFolderService.deleteFileRule(fileId, agentId, folderId, groupId,allowedMethod);
    }

    @PostMapping("/deleteFile")
    public Body<String> deleteFile(@RequestParam("fileId") Long fileId,
            @RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId) {
        return fileFolderService.deleteFile(fileId, agentId, folderId, my.getBase_path());
    }

    @PostMapping("/sendFile")
    public ResponseEntity<Resource> sendFile(@RequestParam("fileId") Long fileId,
            @RequestParam("agentId") Long agentId,
            @RequestParam("folderId") Long folderId) {

        return fileFolderService.sendFile(fileId, agentId, folderId, my.getBase_path());
    }

    @PostMapping ("/getFile")
    public R<File> getFile(@RequestParam("fileId") Long fileId,
                          @RequestParam("agentId") Long agentId) {
        return fileFolderService.getFile(fileId,agentId);
    }

    @PostMapping("/getDirectory")
    public Body<DirectoryInfo> getDirectory(@RequestParam("rootId") Long rootFolderId) {
        DirectoryInfo directory = fileFolderService.getDirectoryStructure(rootFolderId);
        return Body.success(directory, "1");
    }

    @PostMapping("/getDirectoryByGroup")
    public Body<DirectoryInfo> getDirectory(@RequestParam("rootId") Long rootFolderId,
            @RequestParam("agentId") Long agentId,
            @RequestParam("groupId") Long groupId) {
        DirectoryInfo directory = fileFolderService.getDirectoryStructure(rootFolderId);
        directory = fileFolderService.filterFoldersByVisibility(groupId, agentId, directory);
        directory = fileFolderService.filterFilesByRule(groupId, agentId, directory);
        return Body.success(directory, "1");
    }

    @PostMapping("/getDirectoryByApplication")
    public Body<DirectoryInfo> getDirectoryByApplication(@RequestParam("rootId") Long rootFolderId,
            @RequestParam("agentId") Long agentId,
            @RequestParam("applicationId") Long applicationId) {
        DirectoryInfo directory = fileFolderService.getDirectoryStructure(rootFolderId);
        List<Long> groupIds = fileFolderService.getGroupIdsByApplication(applicationId, agentId);
        directory = fileFolderService.filterFolders(groupIds, agentId, directory);
        directory = fileFolderService.filterFiles(groupIds, agentId, directory);
        return Body.success(directory, "1");
    }

    // 同步
    @PostMapping("/newAgent")
    public ResponseEntity<String> newAgent(@RequestParam("agentId") Long agentId,
            @RequestParam("name") String name) {

        Agent agent = new Agent();
        agent.setName(name);
        agent.setUid(agentId);
        fileFolderService.addAgent(agent);
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
        fileFolderService.addAgent(agent);
        return ResponseEntity.ok("File synchronized successfully");
    }

    @GetMapping("/test")
    public String test(){
        return fileFolderService.test();
    }

    @PostMapping("/getRowCount")
    public Body<Long> getRowCount(@RequestParam("fileId") Long fileId,
                                  @RequestParam("agentId")Long agentId){
        return fileFolderService.getRowCount(fileId,agentId,my.getBase_path());
    }



}
