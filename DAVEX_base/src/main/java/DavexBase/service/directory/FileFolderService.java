package DavexBase.service.directory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import DavexBase.service.MQ.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import DavexBase.common.Body;
import DavexBase.common.GetMaxUid;
import DavexBase.entity.Agent;
import DavexBase.entity.ApplicationGroup;
import DavexBase.entity.File;
import DavexBase.entity.Folder;
import DavexBase.entity.FolderVisibility;
import DavexBase.entity.RabbitmqConnection;
import DavexBase.info.DirectoryInfo;
import DavexBase.info.FileInfo;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.ApplicationGroupMapper;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.FolderMapper;
import DavexBase.mapper.FolderVisibilityMapper;
import DavexBase.mapper.RabbitmqConnectionMapper;
import DavexBase.service.MQ.PublishService;
import DavexBase.service.auth.CenterWebClientService;

@Service
public class FileFolderService {

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private ApplicationGroupMapper applicationGroupMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private RabbitmqConnectionMapper rabbitmqConnectionMapper;

    @Autowired
    private FolderVisibilityMapper folderVisibilityMapper;

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    PublishService publishService;

    @Autowired
    MessageService messageService;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FileFolderService() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // 创建文件夹
    public Body<String> createFolder(String name, String path, String agent_id, String parent_id) {
        // 1.检查是否父文件夹存在
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, parent_id);
        Folder fatherFolder = folderMapper.selectOne(queryWrapper);
        if (fatherFolder == null) {
            return Body.error("父文件夹不存在");
        }
        queryWrapper.clear();

        // 2.查询数据库相同父文件夹下是否有同名文件夹
        queryWrapper.eq(Folder::getParentId, parent_id).eq(Folder::getName, name);
        List<Folder> folderList = folderMapper.selectList(queryWrapper);
        if (!folderList.isEmpty()) {
            return Body.error("重名文件夹");
        }

        // 3.本地创建新文件夹
        Path create_path = Path.of(getFolderPath(fatherFolder, path) + "/" + name);
        try {
            Files.createDirectories(create_path);
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error("文件夹创建失败: " + e.getMessage());
        }

        // 3.新文件夹插入
        Folder new_folder = new Folder();
        GetMaxUid getMaxUid = new GetMaxUid();
        //
        int maxTailNumber = getMaxUid.getFolderMaxUid(agent_id, folderMapper);
        String uid = agent_id + "-F" + (maxTailNumber + 1);
        new_folder.setUid(uid);
        new_folder.setName(name);
        new_folder.setAgentId(agent_id);
        new_folder.setParentId(parent_id);
        new_folder.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
        new_folder.setLastUpdate(Timestamp.valueOf(LocalDateTime.now()));
        folderMapper.insert(new_folder);

        //通信
        String exchange = "FileExchange";
        String message = null;
        String operation = "Create";
        String entity = "folder";
        message = messageService.getMessage(operation,entity,objectMapper,new_folder);
        //
        List<Object> uidList = rabbitmqConnectionMapper.selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        for(Object obj : uidList){
            if (obj instanceof String){
                String targetId = (String) obj;
                publishService.sendMessageToFanoutExchange(targetId,exchange,message);
            }
        }
        return Body.success("插入新文件夹成功");
    }

    // 修改文件夹名
    public Body<String> setFolderName(String agentId, String folderId, String name, String baseDirectory) {
        // 1. 首先查找该文件夹是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }

        // 2. 得到父文件夹的 ID 列表，并构建本地绝对路径
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);

        // 构建文件夹路径
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();

        // 构建新的文件夹路径
        String newFolderPath = fullPathBuilder.substring(0, fullPathBuilder.lastIndexOf(java.io.File.separator))
                + java.io.File.separator + name;

        // 3. 修改数据库 folder 表
        folder.setName(name);
        folder.setAgentId(agentId);
        folder.setUid(folderId);
        UpdateWrapper<Folder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("agent_id", agentId)
                .eq("uid", folderId);
        folderMapper.update(folder, updateWrapper);

        // 4. 实际修改本地文件夹名
        java.io.File oldFolder = new java.io.File(oldFolderPath);
        java.io.File newFolder = new java.io.File(newFolderPath);
        if (oldFolder.exists() && oldFolder.isDirectory()) {
            boolean renamed = oldFolder.renameTo(newFolder);
            if (!renamed) {
                return Body.error("本地文件夹重命名失败");
            }
        } else {
            return Body.error("本地文件夹不存在或不是一个目录");
        }

        //通信
        String exchange = "FileExchange";
        String message = null;
        String operation = "Update";
        String entity = "folder";
        message = messageService.getMessage(operation,entity,objectMapper,folder);
        //
        List<Object> uidList = rabbitmqConnectionMapper.selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        for(Object obj : uidList){
            if (obj instanceof String){
                String targetId = (String) obj;
                publishService.sendMessageToFanoutExchange(targetId,exchange,message);
            }
        }

        return Body.success("文件夹名称更新成功");

    }

    // 删除文件夹
    public Body<String> deleteFolder(String agentId, String folderId, String baseDirectory) {
        // 1.查找是否存在或者有子文件夹与文件，如果有则无法删除
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        // 检查是否有子文件夹
        List<String> subFolderIds = new ArrayList<>();
        findAllChildFolders(folderId, agentId, subFolderIds);
        if (!subFolderIds.isEmpty()) {
            return Body.error("该文件夹包含子文件夹，无法删除");
        }

        // 检查文件夹是否包含文件
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getFolderId, folderId);
        if (fileMapper.selectCount(queryFileWrapper) > 0) {
            return Body.error("该文件夹包含文件，无法删除");
        }

        // 2.先得到文件夹路径，再从数据库中删除folder
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();

        folderMapper.delete(queryFolderWrapper);
        // 3.本地实际删除folder
        java.io.File oldFolder = new java.io.File(oldFolderPath);
        if (oldFolder.exists() && oldFolder.isDirectory()) {
            boolean deleted = oldFolder.delete();
            if (!deleted) {
                return Body.error("本地文件夹删除失败");
            }
        } else {
            return Body.error("本地文件夹不存在或不是一个目录");
        }

        //通信
        String exchange = "FileExchange";
        String message = null;
        String operation = "Delete";
        String entity = "folder";
        message = messageService.getMessage(operation,entity,objectMapper,folder);
        //
        List<Object> uidList = rabbitmqConnectionMapper.selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        for(Object obj : uidList){
            if (obj instanceof String){
                String targetId = (String) obj;
                publishService.sendMessageToFanoutExchange(targetId,exchange,message);
            }
        }

        return Body.success("文件夹删除成功");
    }

    // 获取文件夹路径
    public Body<String> getFolderPath(String agentId, String folderId, String baseDirectory) {
        // 1.查找是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        String path = getFolderPath(folder, baseDirectory);
        return Body.success(path, "成功");
    }

    // 展示某文件的详细信息
    public Body<FileInfo> getFileInfo(String uid, String agentId, String folderId) {
        return Body.success(fileMapper.getFileInfo(uid, agentId, folderId), "成功");
    }

    // 上传文件
    public Body<String> uploadFile(String agentId, String folderId, MultipartFile file, String baseDirectory) {
        // 查找是否存在该文件夹
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        // 查找是否存在同名文件
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getName, file.getOriginalFilename())
                .eq(File::getFolderId, folderId);
        if (fileMapper.selectOne(queryFileWrapper) != null) {
            return Body.error("有重名文件");
        }
        // 构建文件夹路径
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getOriginalFilename());
        String folderPath = fullPathBuilder.toString();
        // 将文件存储到实际目录中
        java.io.File destFile = new java.io.File(folderPath);
        try {
            org.apache.commons.io.FileUtils.writeByteArrayToFile(destFile, file.getBytes());
        } catch (IOException e) {
            return Body.error("文件上传失败: " + e.getMessage());
        }
        // 将文件元数据添加到数据库中
        File fileRecord = new File();
        try {
            // 获取文件的byte信息
            byte[] uploadBytes = file.getBytes();
            // 拿到一个SHA-256转换器
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(uploadBytes);
            // 转换为16进制
            fileRecord.setHash(new BigInteger(1, digest).toString(16));
        } catch (Exception e) {
            return Body.error("文件计算hash失败" + e.getMessage());
        }

        String fileName = file.getOriginalFilename();
        //
        GetMaxUid getMaxUid = new GetMaxUid();
        //
        int maxTailNumber = getMaxUid.getFileMaxUid(agentId, fileMapper);
        String uid = agentId + "-D" + (maxTailNumber + 1);
        fileRecord.setUid(uid);
        // fileRecord.setType(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")));
        fileRecord.setAgentId(agentId);
        fileRecord.setFolderId(folderId);
        fileRecord.setName(fileName);
        fileRecord.setSize(file.getSize());

        fileRecord.setCreateDate(new Timestamp(System.currentTimeMillis()));
        fileRecord.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        // 其他元数据设置
        fileMapper.insert(fileRecord);

        //通信
        String exchange = "FileExchange";
        String message = null;
        String operation = "Create";
        String entity = "file";
        message = messageService.getMessage(operation,entity,objectMapper,fileRecord);
        //
        List<Object> uidList = rabbitmqConnectionMapper.selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        for(Object obj : uidList){
            if (obj instanceof String){
                String targetId = (String) obj;
                publishService.sendMessageToFanoutExchange(targetId,exchange,message);
            }
        }

        // 结果
        return Body.success("文件上传成功");
    }

    // 更新文件
    public Body<String> updateFile(File file) {
        //
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, file.getUid())
                .eq(File::getFolderId, file.getFolderId());
        File new_file = fileMapper.selectOne(queryFileWrapper);
        if (new_file == null) {
            return Body.error("该文件不存在");
        }
        new_file.setName(file.getName());
        new_file.setDescription(file.getDescription());
        new_file.setExpiredTime(file.getExpiredTime());
        new_file.setExample(file.getExample());
        new_file.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        new_file.setUid(file.getUid());
        new_file.setAgentId(file.getAgentId());
        new_file.setFolderId(file.getFolderId());
        UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("folder_id", file.getFolderId())
                .eq("uid", file.getUid());
        fileMapper.update(new_file, updateWrapper);

        //通信
        String exchange = "FileExchange";
        String message = null;
        String operation = "Update";
        String entity = "file";
        message = messageService.getMessage(operation,entity,objectMapper,new_file);
        //
        List<Object> uidList = rabbitmqConnectionMapper.selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        for(Object obj : uidList){
            if (obj instanceof String){
                String targetId = (String) obj;
                publishService.sendMessageToFanoutExchange(targetId,exchange,message);
            }
        }
        return Body.success("更新成功");
    }

    // 删除文件
    public Body<String> deleteFile(String fileId, String agentId, String folderId, String baseDirectory) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId);
        File file = fileMapper.selectOne(queryFileWrapper);
        if (file == null) {
            return Body.error("该文件不存在");
        }
        // 构建文件夹路径
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getName());
        String folderPath = fullPathBuilder.toString();

        fileMapper.delete(queryFileWrapper);
        // 3.本地实际删除file
        java.io.File oldFolder = new java.io.File(folderPath);
        if (oldFolder.exists()) {
            boolean deleted = oldFolder.delete();
            if (!deleted) {
                return Body.error("本地文件删除失败");
            }
        } else {
            return Body.error("本地文件不存在或不是一个目录");
        }


        //通信
        String exchange = "FileExchange";
        String message = null;
        String operation = "Delete";
        String entity = "file";
        message = messageService.getMessage(operation,entity,objectMapper,file);
        //
        List<Object> uidList = rabbitmqConnectionMapper.selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        for(Object obj : uidList){
            if (obj instanceof String){
                String targetId = (String) obj;
                publishService.sendMessageToFanoutExchange(targetId,exchange,message);
            }
        }

        return Body.success("文件删除成功");

    }

    //
    public void addAgent(Agent agent) {
        agentMapper.insert(agent);
    }

    private void findAllParentFolders(String folderId, List<String> parentFolderIds,
            List<String> path) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        if (folder != null) {
            path.add(folder.getName());
            if (folder.getParentId() != "-1") {
                parentFolderIds.add(folder.getParentId());
                findAllParentFolders(folder.getParentId(), parentFolderIds, path);
            }
        }
    }

    private void findAllChildFolders(String folderId, String agentId, List<String> childFolderIds) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId, folderId);
        List<Folder> childFolders = folderMapper.selectList(queryWrapper);
        for (Folder folder : childFolders) {
            childFolderIds.add(folder.getUid());
            findAllChildFolders(folder.getUid(), agentId, childFolderIds);
        }
    }

    public String getFolderPath(Folder folder, String baseDirectory) {
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folder.getUid(), parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();
        return oldFolderPath;
    }

    public String getFilePath(File file, String baseDirectory) {

        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, file.getFolderId());
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        String folderPath = getFolderPath(folder, baseDirectory);
        String filePath = folderPath + "/" + file.getName();
        return filePath;
    }

    public DirectoryInfo getDirectoryStructure(String rootFolderId) {
        Folder rootFolder = folderMapper.select(rootFolderId);
        DirectoryInfo root = new DirectoryInfo();
        mapFolderToDirectoryNode(rootFolder, root);
        root.setType("folder");
        buildDirectoryTree(root);
        return root;
    }

    private void buildDirectoryTree(DirectoryInfo node) {
        List<Folder> subFolders = folderMapper.selectByParentId(node.getUid());
        for (Folder subFolder : subFolders) {
            DirectoryInfo childNode = new DirectoryInfo();
            mapFolderToDirectoryNode(subFolder, childNode);
            childNode.setType("folder");
            node.getChildren().add(childNode);
            buildDirectoryTree(childNode);
        }

        List<File> files = fileMapper.selectByFolderId(node.getUid());
        for (File file : files) {
            DirectoryInfo childNode = new DirectoryInfo();
            mapFileToDirectoryNode(file, childNode);
            childNode.setType("file");
            node.getChildren().add(childNode);
        }
    }

    //
    private void mapFolderToDirectoryNode(Folder folder, DirectoryInfo node) {
        node.setUid(folder.getUid());
        node.setParentId(folder.getParentId());
        node.setName(folder.getName());
        node.setCreateDate(folder.getCreateDate());
        node.setLastUpdate(folder.getLastUpdate());
    }

    //
    private void mapFileToDirectoryNode(File file, DirectoryInfo node) {
        node.setUid(file.getUid());
        node.setParentId(file.getFolderId());
        node.setName(file.getName());
        node.setCreateDate(file.getCreateDate());
        node.setLastUpdate(file.getLastUpdate());
        node.setAttribute(file.getAttribute());
        node.setSize(file.getSize());
        node.setDescription(file.getDescription());
        node.setHash(file.getHash());
        node.setExample(file.getExample());
        node.setExpiredTime(file.getExpiredTime());
    }
    //

    public DirectoryInfo filterFoldersByVisibility(Long groupId, String agentId, DirectoryInfo directoryAll) {
        return filterFoldersRecursive(groupId, agentId, directoryAll);
    }

    //
    public DirectoryInfo filterFolders(List<Long> groupIds, String agentId, DirectoryInfo directory) {
        return filterFoldersRecursive(groupIds, agentId, directory);
    }

    //
    private DirectoryInfo filterFoldersRecursive(Long groupId, String agentId, DirectoryInfo directory) {
        List<DirectoryInfo> visibleChildren = new ArrayList<>();
        for (DirectoryInfo child : directory.getChildren()) {
            if ("folder".equals(child.getType())) {
                FolderVisibility visibility = folderVisibilityMapper.selectOne(
                        new QueryWrapper<FolderVisibility>()
                                .eq("group_id", groupId)
                                .eq("agent_id", agentId)
                                .eq("folder_id", child.getUid()));
                if (visibility != null) {
                    visibleChildren.add(filterFoldersRecursive(groupId, agentId, child));
                }
            } else {
                visibleChildren.add(child);// 将文件直接插入
            }
        }
        directory.setChildren(visibleChildren);
        return directory;
    }

    //
    private DirectoryInfo filterFoldersRecursive(List<Long> groupIds, String agentId, DirectoryInfo directory) {
        List<DirectoryInfo> visibleChildren = new ArrayList<>();
        for (DirectoryInfo child : directory.getChildren()) {
            if ("folder".equals(child.getType())) {
                boolean isVisible = groupIds.stream().anyMatch(groupId -> folderVisibilityMapper.selectCount(
                        new QueryWrapper<FolderVisibility>()
                                .eq("group_id", groupId)
                                .eq("agent_id", agentId)
                                .eq("folder_id", child.getUid())) > 0);
                if (isVisible) {
                    visibleChildren.add(filterFoldersRecursive(groupIds, agentId, child));
                }
            } else {
                visibleChildren.add(child); // 将文件直接插入
            }
        }
        directory.setChildren(visibleChildren);
        return directory;
    }

    //
    public List<Long> getGroupIdsByApplication(String applicationId, String agentId) {
        return applicationGroupMapper.selectList(
                new QueryWrapper<ApplicationGroup>()
                        .eq("application_id", applicationId)
                        .eq("agent_id", agentId))
                .stream().map(ApplicationGroup::getGroupId).collect(Collectors.toList());
    }

    //
    public ResponseEntity<Resource> sendFile(String fileId, String agentId, String folderId, String baseDirectory) {

        LambdaQueryWrapper<File> queryFolderWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId);
        File file = fileMapper.selectOne(queryFolderWrapper);
        String filePath = getFilePath(file, baseDirectory);

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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);

    }

    public Body<File> getFile(String fileId, String agentId) {

        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery().eq(File::getUid, fileId);
        File file = fileMapper.selectOne(queryWrapper);
        return Body.success(file, "查询成功");
    }

    public String test() {
        return "1";
    }

    public Body<Long> getRowCount(String fileId, String agentId, String baseDirectory) {
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId);
        File file = fileMapper.selectOne(queryWrapper);
        if (file == null) {
            return Body.error("找不到文件");
        }
        if (!file.getType().equals(".csv")) {
            return Body.error("非csv文件");
        } else {
            String filePath = getFilePath(file, baseDirectory);
            Long rowCount = Long.valueOf(0);
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                while (reader.readLine() != null) {
                    rowCount++;
                }
            } catch (IOException e) {
                return Body.error("读取文件时出错：" + e.getMessage());
            }
            return Body.success(rowCount, "成功获取行数");
        }
    }

    public Body<Folder> getFolder(String folderId, String agentId) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery().eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        return Body.success(folder, "查询成功");
    }

    public Body<DirectoryInfo> getDirectory2Agent(String agentId, String applicationId) {
        try {
            Body<DirectoryInfo> response = centerWebClientService.center2AgentWebClient(agentId).post()
                    .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getDirectoryByApplication")
                            .queryParam("rootId", 1)
                            .queryParam("agentId", agentId)
                            .queryParam("applicationId", applicationId)
                            .build())// 将请求体设置为QueryRequest
                    .retrieve() // 准备接收响应
                    .bodyToMono(new ParameterizedTypeReference<Body<DirectoryInfo>>() {
                    }) // 指定返回类型
                    .block(); // 阻塞等待响应并获取结果
            return response;
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }
}
