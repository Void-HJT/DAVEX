package DveAgent.module.directory;

import DveAgent.common.Body;
import DveAgent.common.R;
import DveAgent.entity.*;
import DveAgent.info.DirectoryInfo;
import DveAgent.info.FileInfo;
import DveAgent.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileFolderService {


    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private ApplicationGroupMapper applicationGroupMapper;

    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private FileRuleMapper fileRuleMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private FolderVisibilityMapper folderVisibilityMapper;


    public Body getFileByRuleOrNot(Long agentId,Long applicationId, Long fileId,String method) {

        boolean isAllowed = false;

        List<ApplicationGroup> applicationGroups = applicationGroupMapper.selectList(
                new QueryWrapper<ApplicationGroup>()
                        .eq("agent_id",agentId)
                        .eq("application_id", applicationId)
        );


        List<FileRule> fileRules = fileRuleMapper.selectList(
                new QueryWrapper<FileRule>()
                        .eq("agent_id", agentId)
                        .eq("file_id", fileId)
        );
        List<Long> ruleIds = new LinkedList<>();

        for (FileRule fileRule : fileRules)
        {
            if(!ruleIds.contains(fileRule.getRuleId()))
            {
                ruleIds.add(fileRule.getRuleId());
            }
        }

        List<Rule> rules = ruleMapper.selectBatchIds(ruleIds);


        for (Rule rule : rules) {

            boolean groupMatch = applicationGroups.stream()
                    .anyMatch(group -> group.getGroupId().equals(rule.getGroupId()));

            if (groupMatch && method.equals(rule.getAllowedMethod())) {
                isAllowed = true;
                break;
            }
        }

        return Body.success(isAllowed,"");
    }

    //创建文件夹
    public Body<String> createFolder(String name, String path,Long agent_id, Long parent_id){
        //1.检查是否父文件夹存在
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, parent_id)
                .eq(Folder::getAgentId,agent_id);
        Folder fatherFolder = folderMapper.selectOne(queryWrapper);
        if(fatherFolder ==null){return Body.error("父文件夹不存在");}
        queryWrapper.clear();

        //2.查询数据库相同父文件夹下是否有同名文件夹
        queryWrapper.eq(Folder::getAgentId,agent_id).eq(Folder::getParentId,parent_id).eq(Folder::getName,name);
        List<Folder> folderList = folderMapper.selectList(queryWrapper);
        if(!folderList.isEmpty()){return Body.error("重名文件夹");}

        //3.本地创建新文件夹
        Path create_path = Path.of(getFolderPath(fatherFolder, path)+"/"+name);
        try {
            Files.createDirectories(create_path);
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error("文件夹创建失败: " + e.getMessage());
        }

        //3.新文件夹插入
        Folder new_folder = new Folder();
        new_folder.setName(name);new_folder.setAgentId(agent_id);new_folder.setParentId(parent_id);
        new_folder.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));new_folder.setLastUpdate(Timestamp.valueOf(LocalDateTime.now()));
        folderMapper.insert(new_folder);

        return Body.success("插入新文件夹成功");
    }
    //设置文件夹可见或不可见
    public Body<String> setFolderVisible(Long agentId, Long groupId, Long folderId) {
        //1.判断用户组 文件夹是否存在
        LambdaQueryWrapper<Group> queryGroupWrapper = Wrappers.<Group>lambdaQuery()
                .eq(Group::getUid,groupId)
                .eq(Group::getAgentId,agentId);
        if(groupMapper.selectList(queryGroupWrapper).isEmpty()){return Body.error("该用户组不存在");}
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid,folderId)
                .eq(Folder::getAgentId,agentId);
        if(folderMapper.selectList(queryFolderWrapper).isEmpty()){return Body.error("该文件夹不存在");}
        //2.寻找其所有父文件夹，如果有一个父文件夹对该用户不可见，那么返回错误（当父文件不可见，子文件夹可见不合理）
        List<Long> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds,path);

        for (Long parentId : parentFolderIds) {
            LambdaQueryWrapper<FolderVisibility> queryVisibilityWrapper = Wrappers.<FolderVisibility>lambdaQuery()
                    .eq(FolderVisibility::getFolderId, parentId)
                    .eq(FolderVisibility::getGroupId, groupId)
                    .eq(FolderVisibility::getAgentId, agentId);
            if (folderVisibilityMapper.selectList(queryVisibilityWrapper).isEmpty()) {
                return Body.error("父文件夹中存在对该用户组不可见的文件夹，设置失败");
            }
        }
        // 3. 将当前文件夹设置为可见
        FolderVisibility folderVisibility = new FolderVisibility();
        folderVisibility.setAgentId(agentId);
        folderVisibility.setFolderId(folderId);
        folderVisibility.setGroupId(groupId);
        folderVisibilityMapper.insert(folderVisibility);
        return Body.success("设置成功");

    }
    public Body<String> setFolderInvisible(Long agentId, Long groupId, Long folderId) {
        // 1. 查表判断用户组与文件夹可见性是否存在
        LambdaQueryWrapper<FolderVisibility> queryVisibilityWrapper = Wrappers.<FolderVisibility>lambdaQuery()
                .eq(FolderVisibility::getFolderId, folderId)
                .eq(FolderVisibility::getGroupId, groupId)
                .eq(FolderVisibility::getAgentId, agentId);
        if (folderVisibilityMapper.selectList(queryVisibilityWrapper).isEmpty()) {
            return Body.error("该文件夹对该用户组本来就是不可见的");
        }

        // 2. 寻找其所有子文件夹，如果子文件夹对该用户组可见，将其设为不可见
        List<Long> childFolderIds = new ArrayList<>();
        findAllChildFolders(folderId, agentId, childFolderIds);

        for (Long childId : childFolderIds) {
            LambdaQueryWrapper<FolderVisibility> queryChildVisibilityWrapper = Wrappers.<FolderVisibility>lambdaQuery()
                    .eq(FolderVisibility::getFolderId, childId)
                    .eq(FolderVisibility::getGroupId, groupId)
                    .eq(FolderVisibility::getAgentId, agentId);
            List<FolderVisibility> childVisibilities = folderVisibilityMapper.selectList(queryChildVisibilityWrapper);
            for (FolderVisibility childVisibility : childVisibilities) {
                folderVisibilityMapper.delete(new QueryWrapper<FolderVisibility>().eq("uid", childVisibility.getUid()));
            }
        }

        // 3. 将该文件夹设为不可见
        FolderVisibility folderVisibility = folderVisibilityMapper.selectOne(queryVisibilityWrapper);
        if (folderVisibility != null) {
            folderVisibilityMapper.delete(new QueryWrapper<FolderVisibility>().eq("uid", folderVisibility.getUid()));
        }

        return Body.success("设置成功");
    }
    //修改文件夹名
    public Body<String> setFolderName(Long agentId, Long folderId, String name ,String baseDirectory) {
        // 1. 首先查找该文件夹是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }

        // 2. 得到父文件夹的 ID 列表，并构建本地绝对路径
        List<Long> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds, path);

        // 构建文件夹路径
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
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
        UpdateWrapper<Folder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("agent_id",agentId)
                .eq("uid",folderId);
        folderMapper.update(folder,updateWrapper);

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

        return Body.success("文件夹名称更新成功");

    }
    //删除文件夹
    public Body<String> deleteFolder(Long agentId, Long folderId,String baseDirectory) {
        //1.查找是否存在或者有子文件夹与文件，如果有则无法删除
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        // 检查是否有子文件夹
        List<Long> subFolderIds = new ArrayList<>();
        findAllChildFolders(folderId, agentId, subFolderIds);
        if (!subFolderIds.isEmpty()) {
            return Body.error("该文件夹包含子文件夹，无法删除");
        }

        // 检查文件夹是否包含文件
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        if (fileMapper.selectCount(queryFileWrapper) > 0) {
            return Body.error("该文件夹包含文件，无法删除");
        }

        //2.先得到文件夹路径，再从数据库中删除folder
        List<Long> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();

        folderMapper.delete(queryFolderWrapper);
        //3.本地实际删除folder
        java.io.File oldFolder = new java.io.File(oldFolderPath);
        if (oldFolder.exists() && oldFolder.isDirectory()) {
            boolean deleted = oldFolder.delete();
            if (!deleted) {
                return Body.error("本地文件夹删除失败");
            }
        } else {
            return Body.error("本地文件夹不存在或不是一个目录");
        }
        return Body.success("文件夹删除成功");
    }
    //获取文件夹路径
    public Body<String> getFolderPath(Long agentId, Long folderId, String baseDirectory) {
        //1.查找是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        String path = getFolderPath(folder,baseDirectory);
        return Body.success(path,"成功");
    }
    //展示某文件的详细信息
    public Body<FileInfo> getFileInfo(Long uid, Long agentId, Long folderId) {
        return Body.success(fileMapper.getFileInfo(uid, agentId, folderId),"成功");
    }

    //上传文件
    public Body<String> uploadFile(Long agentId, Long folderId, MultipartFile file, String baseDirectory) {
        //查找是否存在该文件夹
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        //查找是否存在同名文件
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getName, file.getOriginalFilename())
                .eq(File::getAgentId, agentId)
                .eq(File::getFolderId,folderId);
        if(fileMapper.selectOne(queryFileWrapper)!=null){return Body.error("有重名文件");}
        // 构建文件夹路径
        List<Long> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds, path);
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getOriginalFilename());
        String folderPath = fullPathBuilder.toString();
        //将文件存储到实际目录中
        java.io.File destFile = new java.io.File(folderPath);
        try {
            org.apache.commons.io.FileUtils.writeByteArrayToFile(destFile, file.getBytes());
        } catch (IOException e) {
            return Body.error("文件上传失败: " + e.getMessage());
        }

        File fileRecord = new File();
        String hash = getSha256(file);
        String fileName = file.getOriginalFilename();
        fileRecord.setHash(hash);
        fileRecord.setAgentId(agentId);
        fileRecord.setFolderId(folderId);
        fileRecord.setName(fileName);
        fileRecord.setSize(file.getSize());
        fileRecord.setType(fileName.substring(fileName.lastIndexOf(".") + 1));
        fileRecord.setCreateDate(new java.sql.Timestamp(System.currentTimeMillis()));
        fileRecord.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
        // 其他元数据设置
        fileMapper.insert(fileRecord);

        // 结果
        return Body.success("文件上传成功");
    }
    //更新文件
    public Body<String> updateFile(File file) {
        //
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid,file.getUid())
                .eq(File::getAgentId,file.getAgentId())
                .eq(File::getFolderId,file.getFolderId());
        File new_file = fileMapper.selectOne(queryFileWrapper);
        if(new_file==null){return Body.error("该文件不存在");}
        new_file.setName(file.getName()+"."+file.getType());
        new_file.setDescription(file.getDescription());
        new_file.setExpiredTime(file.getExpiredTime());
        new_file.setExample(file.getExample());
        new_file.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
        UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("agent_id",file.getAgentId())
                .eq("folder_id",file.getFolderId())
                .eq("uid",file.getUid());
        fileMapper.update(new_file,updateWrapper);
        return Body.success("更新成功");
    }
    //设置文件规则
    public Body<String> setFileRule(Long fileId, Long agentId, Long folderId, Long ruleId) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid,fileId)
                .eq(File::getAgentId,agentId)
                .eq(File::getFolderId,folderId);
        if(fileMapper.selectOne(queryFileWrapper)==null){return Body.error("该文件不存在");}
        LambdaQueryWrapper<Rule> queryRuleWrapper = Wrappers.<Rule>lambdaQuery()
                .eq(Rule::getUid,ruleId)
                .eq(Rule::getAgentId,agentId);
        if(ruleMapper.selectOne(queryRuleWrapper)==null){return Body.error("该规则不存在");}
        LambdaQueryWrapper<FileRule> queryFileRuleWrapper = Wrappers.<FileRule>lambdaQuery()
                .eq(FileRule::getAgentId,agentId)
                .eq(FileRule::getFileId,fileId)
                .eq(FileRule::getRuleId,ruleId);
        if(fileRuleMapper.selectOne(queryFileRuleWrapper)!=null){return Body.error("该关系已存在");}
        FileRule fileRule = new FileRule();
        fileRule.setFileId(fileId);fileRule.setRuleId(ruleId);fileRule.setAgentId(agentId);
        fileRuleMapper.insert(fileRule);
        return Body.success("成功插入");
    }
    //删除文件
    public Body<String> deleteFile(Long fileId, Long agentId, Long folderId,String baseDirectory) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid,fileId)
                .eq(File::getAgentId,agentId)
                .eq(File::getFolderId,folderId);
        File file = fileMapper.selectOne(queryFileWrapper);
        if(file==null){return Body.error("该文件不存在");}
        // 构建文件夹路径
        List<Long> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds, path);
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getName());
        String folderPath = fullPathBuilder.toString();

        fileMapper.delete(queryFileWrapper);
        //3.本地实际删除file
        java.io.File oldFolder = new java.io.File(folderPath);
        if (oldFolder.exists()) {
            boolean deleted = oldFolder.delete();
            if (!deleted) {
                return Body.error("本地文件删除失败");
            }
        } else {
            return Body.error("本地文件不存在或不是一个目录");
        }
        return Body.success("文件删除成功");

    }

    //
    public void addAgent(Agent agent) {
        agentMapper.insert(agent);
    }


    private void findAllParentFolders(Long folderId, Long agentId, List<Long> parentFolderIds, List<String> path) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        if (folder != null ) {
            path.add (folder.getName());
            if(folder.getParentId()!=-1){
                parentFolderIds.add(folder.getParentId());
                findAllParentFolders(folder.getParentId(), agentId, parentFolderIds,path);
            }
        }
    }

    private void findAllChildFolders(Long folderId, Long agentId, List<Long> childFolderIds) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId, folderId)
                .eq(Folder::getAgentId, agentId);
        List<Folder> childFolders = folderMapper.selectList(queryWrapper);
        for (Folder folder : childFolders) {
            childFolderIds.add(folder.getUid());
            findAllChildFolders(folder.getUid(), agentId, childFolderIds);
        }
    }

    public String getFolderPath(Folder folder,String baseDirectory){
        List<Long> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folder.getUid(), folder.getAgentId(), parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();
        return oldFolderPath;
    }

    public String getFilePath(File file,String baseDirectory) {

        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, file.getFolderId())
                .eq(Folder::getAgentId, file.getAgentId());
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        String folderPath = getFolderPath(folder,baseDirectory);
        String filePath = folderPath+"/"+file.getName();
        return filePath;
    }


    public DirectoryInfo getDirectoryStructure(Long rootFolderId) {
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
        node.setAgentId(folder.getAgentId());
        node.setParentId(folder.getParentId());
        node.setName(folder.getName());
        node.setCreateDate(folder.getCreateDate());
        node.setLastUpdate(folder.getLastUpdate());
    }
    //
    private void mapFileToDirectoryNode(File file, DirectoryInfo node) {
        node.setUid(file.getUid());
        node.setAgentId(file.getAgentId());
        node.setParentId(file.getFolderId());
        node.setName(file.getName());
        node.setCreateDate(file.getCreateDate());
        node.setLastUpdate(file.getLastUpdate());
        node.setTag(file.getTag());
        node.setSize(file.getSize());
        node.setDescription(file.getDescription());
        node.setHash(file.getHash());
        node.setExample(file.getExample());
        node.setExpiredTime(file.getExpiredTime());
    }
    //

    public DirectoryInfo filterFoldersByVisibility(Long groupId, Long agentId, DirectoryInfo directoryAll) {
        return filterFoldersRecursive(groupId, agentId, directoryAll);
    }
    //
    public DirectoryInfo filterFilesByRule(Long groupId, Long agentId, DirectoryInfo directoryFiltered) {
        return filterFilesRecursive(groupId, agentId, directoryFiltered);
    }
    //
    public DirectoryInfo filterFolders(List<Long> groupIds, Long agentId, DirectoryInfo directory) {
        return filterFoldersRecursive(groupIds, agentId, directory);
    }
    //
    public DirectoryInfo filterFiles(List<Long> groupIds, Long agentId, DirectoryInfo directory) {
        return filterFilesRecursive(groupIds, agentId, directory);
    }
    //
    private DirectoryInfo filterFoldersRecursive(Long groupId, Long agentId, DirectoryInfo directory) {
        List<DirectoryInfo> visibleChildren = new ArrayList<>();
        for (DirectoryInfo child : directory.getChildren()) {
            if ("folder".equals(child.getType())) {
                FolderVisibility visibility = folderVisibilityMapper.selectOne(
                        new QueryWrapper<FolderVisibility>()
                                .eq("group_id", groupId)
                                .eq("agent_id", agentId)
                                .eq("folder_id", child.getUid())
                );
                if (visibility != null) {
                    visibleChildren.add(filterFoldersRecursive(groupId, agentId, child));
                }
            } else {
                visibleChildren.add(child);//将文件直接插入
            }
        }
        directory.setChildren(visibleChildren);
        return directory;
    }
    //
    private DirectoryInfo filterFoldersRecursive(List<Long> groupIds, Long agentId, DirectoryInfo directory) {
        List<DirectoryInfo> visibleChildren = new ArrayList<>();
        for (DirectoryInfo child : directory.getChildren()) {
            if ("folder".equals(child.getType())) {
                boolean isVisible = groupIds.stream().anyMatch(groupId ->
                        folderVisibilityMapper.selectCount(
                                new QueryWrapper<FolderVisibility>()
                                        .eq("group_id", groupId)
                                        .eq("agent_id", agentId)
                                        .eq("folder_id", child.getUid())
                        ) > 0
                );
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
    private DirectoryInfo filterFilesRecursive(Long groupId, Long agentId, DirectoryInfo directory) {
        List<DirectoryInfo> allowedChildren = new ArrayList<>();
        for (DirectoryInfo child : directory.getChildren()) {
            if ("folder".equals(child.getType())) {
                allowedChildren.add(filterFilesRecursive(groupId, agentId, child));
            } else {
                // 检查文件与用户组的关系
                List<FileRule> fileRules = fileRuleMapper.selectList(
                        new QueryWrapper<FileRule>()
                                .eq("agent_id", agentId)
                                .eq("file_id", child.getUid())
                );
                boolean isAllowed = false;
                for (FileRule fileRule : fileRules) {
                    Rule rule = ruleMapper.selectOne(
                            new QueryWrapper<Rule>()
                                    .eq("uid", fileRule.getRuleId())
                                    .eq("group_id", groupId)
                    );
                    if (rule != null) {
                        isAllowed = true;
                        child.getRuleList().add(rule.getAllowedMethod());
                    }
                }
                if (isAllowed) {
                    allowedChildren.add(child);
                }
            }
        }
        directory.setChildren(allowedChildren);
        return directory;
    }
    //
    private DirectoryInfo filterFilesRecursive(List<Long> groupIds, Long agentId, DirectoryInfo directory) {
        List<DirectoryInfo> allowedChildren = new ArrayList<>();
        for (DirectoryInfo child : directory.getChildren()) {
            if ("folder".equals(child.getType())) {
                allowedChildren.add(filterFilesRecursive(groupIds, agentId, child));
            } else {
                // 检查文件与用户组的关系
                List<FileRule> fileRules = fileRuleMapper.selectList(
                        new QueryWrapper<FileRule>()
                                .eq("agent_id", agentId)
                                .eq("file_id", child.getUid())
                );
                List<String> allowedMethods = new ArrayList<>();
                for (FileRule fileRule : fileRules) {
                    List<Rule> rules = ruleMapper.selectList(
                            new QueryWrapper<Rule>()
                                    .eq("uid", fileRule.getRuleId())
                                    .in("group_id", groupIds)
                    );
                    allowedMethods.addAll(rules.stream().map(Rule::getAllowedMethod).collect(Collectors.toList()));
                }
                if (!allowedMethods.isEmpty()) {
                    allowedMethods = allowedMethods.stream().distinct().collect(Collectors.toList());
                    child.setRuleList(allowedMethods);
                    allowedChildren.add(child);
                }
            }
        }
        directory.setChildren(allowedChildren);
        return  directory;
    }
        //
    public List<Long> getGroupIdsByApplication(Long applicationId, Long agentId) {
        return applicationGroupMapper.selectList(
                new QueryWrapper<ApplicationGroup>()
                        .eq("application_id", applicationId)
                        .eq("agent_id", agentId)
        ).stream().map(ApplicationGroup::getGroupId).collect(Collectors.toList());
    }
    //
    public ResponseEntity<Resource> sendFile (Long fileId, Long agentId, Long folderId, String baseDirectory) {


        LambdaQueryWrapper<File> queryFolderWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid,fileId)
                .eq(File::getAgentId,agentId)
                .eq(File::getFolderId,folderId);
        File file = fileMapper.selectOne(queryFolderWrapper);
        String filePath = getFilePath(file,baseDirectory);

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

    public R<File> getFile(Long fileId, Long agentId) {

        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery().eq(File::getUid,fileId).eq(File::getAgentId,agentId);
        File file = fileMapper.selectOne(queryWrapper);
        return R.success(file,"查询成功");
    }

    public String getSha256(MultipartFile file) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(file.getBytes());
            byte[] digest = md.digest();
            String mySha256 = DatatypeConverter
                    .printHexBinary(digest).toLowerCase();

            return mySha256;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
