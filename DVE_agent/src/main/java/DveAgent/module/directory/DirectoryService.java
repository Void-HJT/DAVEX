package DveAgent.module.directory;

import DveAgent.common.Body;
import DveAgent.entity.*;
import DveAgent.info.ApplicationInfo;
import DveAgent.info.FileInfo;
import DveAgent.mapper.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList("direct", "psi", "pir", "mpc"));

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationGroupMapper applicationGroupMapper;

    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private FileRuleMapper fileRuleMapper;

    @Autowired
    private FolderVisibilityMapper folderVisibilityMapper;

    public Body<String> createFolder(String name, String path,Integer agent_id, Integer parent_id){
        //1.查询数据库相同父文件夹下是否有同名文件夹
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId, parent_id)
                .eq(Folder::getName, name);
        List<Folder> folderList = folderMapper.selectList(queryWrapper);
        if(!folderList.isEmpty()){return Body.error("重名文件夹");}

        //2.本地创建新文件夹
        Path create_path = Paths.get(path,name);
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

    public Body<String> addGroup(Integer agentId, Integer centerId, String name) {
        //1.查询数据库是否有同名组
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.<Group>lambdaQuery()
                .eq(Group::getAgentId,agentId)
                .eq(Group::getCenterId,centerId)
                .eq(Group::getName,name);
        List<Group> groupList =groupMapper.selectList(queryWrapper);
        if(!groupList.isEmpty()){return Body.error("重名组");}
        //2.新组加入
        Group new_group = new Group();
        new_group.setAgentId(agentId);new_group.setCenterId(centerId);new_group.setName(name);
        groupMapper.insert(new_group);

        return Body.success("新增组成功");
    }

    public Body<List<Group>> getGroupByApplicationId(Integer agentId, Integer centerId, Integer applicationId) {
        List<Group> groups = groupMapper.getList(agentId,centerId,applicationId);
        return Body.success(groups,"成功");
    }

    public Body<List<ApplicationInfo>> getApplicationAndGroup(Integer agentId,Integer centerId) {
        List<ApplicationInfo> applicationInfos = applicationMapper.getApplicationInfo(agentId,centerId);

        return Body.success(applicationInfos,"成功");
    }

    public Body<List<Group>> getGroup(Integer agentId, Integer centerId) {
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.<Group>lambdaQuery()
                .eq(Group::getCenterId,centerId)
                .eq(Group::getAgentId,agentId);
        List<Group> groupList = groupMapper.selectList(queryWrapper);
        if(groupList.isEmpty()){return Body.error("没有组");}
        return Body.success(groupList,"成功");
    }

    public Body<String> addApplicationGroup(Integer agentId, Integer centerId, Integer applicationId, Integer groupId) {
        //1.判断是否有分组重复
        LambdaQueryWrapper<ApplicationGroup> queryWrapper = Wrappers.<ApplicationGroup>lambdaQuery()
                .eq(ApplicationGroup::getCenterId,centerId)
                .eq(ApplicationGroup::getAgentId,agentId)
                .eq(ApplicationGroup::getGroupId,groupId)
                .eq(ApplicationGroup::getApplicationId,applicationId);
        List<ApplicationGroup> applicationGroupList = applicationGroupMapper.selectList(queryWrapper);
        if(!applicationGroupList.isEmpty()){return Body.error("分组重复");}
        //2.插入新分组
        ApplicationGroup applicationGroup = new ApplicationGroup();
        applicationGroup.setApplicationId(applicationId);applicationGroup.setGroupId(groupId);
        applicationGroup.setAgentId(agentId);applicationGroup.setCenterId(centerId);
        applicationGroupMapper.insert(applicationGroup);
        return Body.success("分组成功");
    }

    public Body<String> addRule(Integer agentId, Integer groupId, String allowMethod) {
        Rule rule = new Rule();
        //1.判断输入的权限是否是指定的
        if(!ALLOWED_METHODS.contains(allowMethod)){return Body.error("规则种类错误");}
        rule.setAllowedMethod(allowMethod);rule.setAgentId(agentId);rule.setGroupId(groupId);

        //2.判断是否重复
        LambdaQueryWrapper<Rule> queryWrapper = Wrappers.<Rule>lambdaQuery()
                .eq(Rule::getAgentId,agentId)
                .eq(Rule::getGroupId,groupId)
                .eq(Rule::getAllowedMethod,allowMethod);
        if(!ruleMapper.selectList(queryWrapper).isEmpty()){return Body.error("规则重复");}
        //3.插入
        ruleMapper.insert(rule);
        return Body.success("添加规则成功");

    }

    public Body<String> setFolderVisible(Integer agentId, Integer groupId, Integer folderId) {
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
        List<Integer> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds,path);

        for (Integer parentId : parentFolderIds) {
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


    public Body<String> setFolderInvisible(Integer agentId, Integer groupId, Integer folderId) {
        // 1. 查表判断用户组与文件夹可见性是否存在
        LambdaQueryWrapper<FolderVisibility> queryVisibilityWrapper = Wrappers.<FolderVisibility>lambdaQuery()
                .eq(FolderVisibility::getFolderId, folderId)
                .eq(FolderVisibility::getGroupId, groupId)
                .eq(FolderVisibility::getAgentId, agentId);
        if (folderVisibilityMapper.selectList(queryVisibilityWrapper).isEmpty()) {
            return Body.error("该文件夹对该用户组本来就是不可见的");
        }

        // 2. 寻找其所有子文件夹，如果子文件夹对该用户组可见，将其设为不可见
        List<Integer> childFolderIds = new ArrayList<>();
        findAllChildFolders(folderId, agentId, childFolderIds);

        for (Integer childId : childFolderIds) {
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

    public Body<String> setFolderName(Integer agentId, Integer folderId, String name ,String baseDirectory) {
        // 1. 首先查找该文件夹是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }

        // 2. 得到父文件夹的 ID 列表，并构建本地绝对路径
        List<Integer> parentFolderIds = new ArrayList<>();
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

    public Body<String> deleteFolder(Integer agentId, Integer folderId,String baseDirectory) {
        //1.查找是否存在或者有子文件夹与文件，如果有则无法删除
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        // 检查是否有子文件夹
        List<Integer> subFolderIds = new ArrayList<>();
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
        List<Integer> parentFolderIds = new ArrayList<>();
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



    private void findAllParentFolders(Integer folderId, Integer agentId, List<Integer> parentFolderIds, List<String> path) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        if (folder != null ) {
            path.add (folder.getName());
            if(folder.getParentId()!=-1){
                parentFolderIds.add((int)folder.getParentId());
                findAllParentFolders((int) folder.getParentId(), agentId, parentFolderIds,path);
            }
        }
    }

    private void findAllChildFolders(Integer folderId, Integer agentId, List<Integer> childFolderIds) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId, folderId)
                .eq(Folder::getAgentId, agentId);
        List<Folder> childFolders = folderMapper.selectList(queryWrapper);
        for (Folder folder : childFolders) {
            childFolderIds.add((int)folder.getUid());
            findAllChildFolders((int)folder.getUid(), agentId, childFolderIds);
        }
    }

    public Body<String> getFolderPath(Integer agentId, Integer folderId, String baseDirectory) {
        //1.查找是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        List<Integer> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();
        return Body.success(oldFolderPath,"成功");
    }

    public Body<File> showFile(Integer uid,Integer agentId, Integer folderId) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid,uid)
                .eq(File::getAgentId,agentId)
                .eq(File::getFolderId,folderId);
        File new_file = fileMapper.selectOne(queryFileWrapper);
        if(new_file==null){return Body.error("该文件不存在");}
        new_file.setName(new_file.getName().substring(0,new_file.getName().lastIndexOf(".")));
        return Body.success(new_file,"查询成功");
    }

    public Body<String> uploadFile(Integer agentId, Integer folderId, MultipartFile file,String baseDirectory) {
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
        List<Integer> parentFolderIds = new ArrayList<>();
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
            file.transferTo(destFile);
        } catch (IOException e) {
            return Body.error("文件上传失败: " + e.getMessage());
        }
        //将文件元数据添加到数据库中
        File fileRecord = new File();
        String fileName = file.getOriginalFilename();
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

    public Body<FileInfo> getFileInfo(Integer uid, Integer agentId, Integer folderId) {

        return Body.success(fileMapper.getFileInfo(uid, agentId, folderId),"成功");
    }

    public Body<String> setFileRule(Integer fileId, Integer agentId, Integer folderId, Integer ruleId) {
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

    public Body<String> deleteFile(Integer fileId, Integer agentId, Integer folderId,String baseDirectory) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid,fileId)
                .eq(File::getAgentId,agentId)
                .eq(File::getFolderId,folderId);
        File file = fileMapper.selectOne(queryFileWrapper);
        if(file==null){return Body.error("该文件不存在");}
        // 构建文件夹路径
        List<Integer> parentFolderIds = new ArrayList<>();
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

    public String getFilePath(Integer fileId, Integer agentId, Integer folderId, String baseDirectory) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getAgentId, agentId)
                .eq(File::getFolderId,folderId);
        File file = fileMapper.selectOne(queryFileWrapper);
        List<Integer> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, agentId, parentFolderIds, path);
        Collections.reverse(path);  // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getName());
        String filePath = fullPathBuilder.toString();
        return filePath;
    }
}
