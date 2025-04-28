package DavexAgent.module.directory;

import DavexBase.entity.Center;
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
import DavexBase.entity.Agent;
import DavexBase.entity.File;
import DavexBase.entity.Folder;
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

    @PostMapping("/createFolder")
    public Body<String> createFolder(@RequestParam("name") String name,
            @RequestParam("agentId") String agentId,
            @RequestParam("parentId") String parentId) {
        String path = my.getBase_path();
        return fileFolderService.createFolder(name, path, agentId, parentId);
    }

    @PostMapping("/setFolderName")
    public Body<String> setFolderName(@RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId,
            @RequestParam("name") String name) {

        return fileFolderService.setFolderName(agentId, folderId, name, my.getBase_path());
    }

    @PostMapping("/deleteFolder")
    public Body<String> deleteFolder(@RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId) {
        return fileFolderService.deleteFolder(agentId, folderId, my.getBase_path());
    }

    @PostMapping("/getFolderPath")
    public Body<String> getFolderPath(@RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId) {
        return fileFolderService.getFolderPath(agentId, folderId, my.getBase_path());
    }

    // 文件

    @PostMapping("/getFileInfo")
    public Body<FileInfo> getFileInfo(@RequestParam("uid") String uid,
            @RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId) {
        return fileFolderService.getFileInfo(uid, agentId, folderId);
    }

    @PostMapping("/uploadFile")
    public Body<String> uploadFile(@RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId,
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

    @PostMapping("/deleteFile")
    public Body<String> deleteFile(@RequestParam("fileId") String fileId,
            @RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId) {
        return fileFolderService.deleteFile(fileId, agentId, folderId, my.getBase_path());
    }

    @PostMapping("/sendFile")
    public ResponseEntity<Resource> sendFile(@RequestParam("fileId") String fileId,
            @RequestParam("agentId") String agentId,
            @RequestParam("folderId") String folderId,
            @RequestParam(value = "chainMaker", defaultValue = "false") Boolean chainMaker,
            @RequestParam(value = "requestHash", defaultValue = "defaultHash") String requestHash,
            @RequestParam(value = "requestId", defaultValue = "defaultId") String requestId) throws Exception {
        return fileFolderService.sendFile(fileId, agentId, folderId, my.getBase_path(), chainMaker, requestHash, requestId);
    }

    @PostMapping("/getDirectory")
    public Body<DirectoryInfo> getDirectory(@RequestParam("rootId") String rootFolderId) {
        DirectoryInfo directory = fileFolderService.getDirectoryStructure(rootFolderId);
        return Body.success(directory, "1");
    }

    // 同步
    @PostMapping("/newAgent")
    public ResponseEntity<String> newAgent(@RequestParam("agentId") String agentId,
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

    @PostMapping("/addCenter")
    public void addCenter(@RequestBody Center center) {
        fileFolderService.addCenter(center);
    }

    @PostMapping("/syncFolders2Center")
    public void syncFolders2Center(@RequestParam("centerId") String centerId) {
        fileFolderService.syncFolders2Center(centerId);
    }

    @PostMapping("/syncFiles2Center")
    public void syncFiles2Center(@RequestParam("centerId") String centerId) {
        fileFolderService.syncFiles2Center(centerId);
    }

    @GetMapping("/test")
    public String test() {
        return fileFolderService.test();
    }

    @PostMapping("/getRowCount")
    public Body<Long> getRowCount(@RequestParam("fileId") String fileId,
            @RequestParam("agentId") String agentId) {
        return fileFolderService.getRowCount(fileId, agentId, my.getBase_path());
    }

    @PostMapping("/getFile")
    public Body<File> getFile(@RequestParam("fileId") String fileId,
            @RequestParam("agentId") String agentId,
            @RequestParam(value = "chainMaker", defaultValue = "false") Boolean chainMaker,
            @RequestParam(value = "requestHash", defaultValue = "defaultHash") String requestHash,
            @RequestParam(value = "requestId", defaultValue = "defaultId") String requestId) throws Exception {
        return fileFolderService.getFile(fileId, agentId, chainMaker, requestHash, requestId);
    }

    @PostMapping("/getFolder")
    public Body<Folder> getFolder(@RequestParam("folderId") String folderId,
            @RequestParam("agentId") String agentId) {
        return fileFolderService.getFolder(folderId, agentId);
    }

    @PostMapping("/getRoot")
    public Body<Folder> getRoot() {
        return fileFolderService.getRoot();
    }
}
