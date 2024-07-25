package DveAgent.module.directory;

import DveAgent.common.Body;
import DveAgent.entity.ApplicationGroup;
import DveAgent.entity.FileRule;
import DveAgent.entity.Rule;
import DveAgent.mapper.ApplicationGroupMapper;
import DveAgent.mapper.FileRuleMapper;
import DveAgent.mapper.RuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class FileService {

    @Autowired
    ApplicationGroupMapper applicationGroupMapper;

    @Autowired
    FileRuleMapper fileRuleMapper;

    @Autowired
    RuleMapper ruleMapper;

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

}
