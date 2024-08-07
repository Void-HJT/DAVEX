package DveAgent.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.module.auth.service.AuthService;
import DveBase.common.My;
import DveBase.entity.Agent;
import DveBase.mapper.AgentMapper;

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
        if (my.getDveType() != My.DveType.DVE_AGENT) {
            throw new RuntimeException("DVE_AGENT启动失败: DVE_TYPE错误");
        }
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, my.getId());
        Agent old_agent = agentMapper.selectOne(queryWrapper);
        Agent new_agent = (Agent) my.getMyObject();
        if (old_agent == null) {
            agentMapper.insert((Agent) my.getMyObject());
            authService.broacast(new_agent);
        } else if (!old_agent.equals(new_agent)) {
            authService.broacast(new_agent);
            agentMapper.updateById(new_agent);
        }

    }

}
