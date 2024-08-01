package DveAgent.module.directory;


import DveAgent.common.Body;
import DveAgent.entity.Application;
import DveAgent.entity.ApplicationGroup;
import DveAgent.entity.Group;
import DveAgent.entity.Rule;
import DveAgent.mapper.ApplicationGroupMapper;
import DveAgent.mapper.ApplicationMapper;
import DveAgent.mapper.GroupMapper;
import DveAgent.mapper.RuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {

    private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList("direct", "psi", "pir", "mpc"));

    @Autowired
    ApplicationMapper applicationMapper;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    ApplicationGroupMapper applicationGroupMapper;

    @Autowired
    RuleMapper ruleMapper;

    //agent查看用户
    public Body<List<Application>> getApplication() {
        LambdaQueryWrapper<Application> queryWrapper = Wrappers.<Application>lambdaQuery();
        List<Application> applications = applicationMapper.selectList(queryWrapper);
        return Body.success(applications,"返回用户列表");
    }
    //agent根据用户id得到分组
    public Body<List<Group>> getGroupByApplicationId(Long agentId, Long centerId, Long applicationId) {
        List<Group> groups = groupMapper.getList(agentId,centerId,applicationId);
        return Body.success(groups,"成功");
    }
    //显示用户分组
    public Body<List<Group>> getGroup(Long agentId, Long centerId) {
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.<Group>lambdaQuery()
                .eq(Group::getCenterId,centerId)
                .eq(Group::getAgentId,agentId);
        List<Group> groupList = groupMapper.selectList(queryWrapper);
        if(groupList.isEmpty()){return Body.error("没有组");}
        return Body.success(groupList,"成功");
    }
    //添加用户组
    public Body<String> addGroup(Long agentId, Long centerId, String name) {
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
    //删除用户
    public Body<String> deleteGroup(Long agentId, Long centerId, Long groupId) {
        //1.查询数据库是组否存在
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.<Group>lambdaQuery()
                .eq(Group::getUid,groupId)
                .eq(Group::getAgentId,agentId)
                .eq(Group::getCenterId,centerId);
        Group group = groupMapper.selectOne(queryWrapper);
        if(group==null){return Body.error("该组不存在");}
        groupMapper.delete(queryWrapper);
        return Body.success("删除组成功");
    }
    //用户划分组
    public Body<String> addApplicationGroup(Long agentId, Long centerId, Long applicationId, Long groupId) {
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
    //删除用户划分组
    public Body<String> deleteApplicationGroup(Long agentId, Long centerId, Long applicationId,Long groupId) {
        LambdaQueryWrapper<ApplicationGroup> queryWrapper = Wrappers.<ApplicationGroup>lambdaQuery()
                .eq(ApplicationGroup::getApplicationId,applicationId)
                .eq(ApplicationGroup::getGroupId,groupId)
                .eq(ApplicationGroup::getCenterId,centerId)
                .eq(ApplicationGroup::getAgentId,agentId);
        ApplicationGroup applicationGroup = applicationGroupMapper.selectOne(queryWrapper);
        if(applicationGroup==null){return Body.error("该分组关系不存在");}
        applicationGroupMapper.delete(queryWrapper);
        return Body.success("删除分组关系成功");
    }

    //设定用户组的访问权限
    public Body<String> addRule(Long agentId, Long groupId, String allowMethod) {
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

    public Body<List<Rule>> getRuleByGroup(Long agentId, Long groupId) {
        LambdaQueryWrapper<Rule> queryRuleWrapper = Wrappers.<Rule>lambdaQuery()
                .eq(Rule::getGroupId, groupId)
                .eq(Rule::getAgentId, agentId);
        List<Rule> rules = ruleMapper.selectList(queryRuleWrapper);
        return Body.success(rules,"成功");
    }

    public Body<String> deleteRule(Long agentId, Long groupId,String allowMethod) {
        LambdaQueryWrapper<Rule> queryWrapper = Wrappers.<Rule>lambdaQuery()
                .eq(Rule::getGroupId,groupId)
                .eq(Rule::getAllowedMethod,allowMethod)
                .eq(Rule::getAgentId,agentId);
        Rule rule = ruleMapper.selectOne(queryWrapper);
        if(rule==null){return Body.error("该规则不存在");}
        ruleMapper.delete(queryWrapper);
        return Body.success("删除规则成功");
    }
}
