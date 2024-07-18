package DveAgent.module.directory;

import DveAgent.common.Body;
import DveAgent.entity.ApplicationGroup;
import DveAgent.entity.Folder;
import DveAgent.entity.Group;
import DveAgent.entity.Rule;
import DveAgent.info.ApplicationInfo;
import DveAgent.mapper.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public Body<String> createFolder(String name, String path,Integer agent_id, Integer parent_id){
        //1.查询数据库相同父文件夹下是否有同名文件夹
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId,parent_id)
                .eq(Folder::getName,name);
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
}
