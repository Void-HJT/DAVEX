package DveAgent.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.My;
import DveAgent.entity.Agent;
import DveAgent.mapper.AgentMapper;
import DveAgent.module.auth.service.AuthService;

@Configuration
public class BoostConfig {

    @Autowired
    private My my;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AuthService authService;

    @PostConstruct
    private void boost() {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, my.getId());
        Agent old_agent = agentMapper.selectOne(queryWrapper);
        if (old_agent == null) {
            agentMapper.insert(my.getAgent());
            authService.broacast(my.getAgent());
        } else if (!old_agent.equals(my.getAgent())) {
            authService.broacast(my.getAgent());
            agentMapper.updateById(my.getAgent());
        } else {
            my.setAgent(old_agent);
        }

    }

}
