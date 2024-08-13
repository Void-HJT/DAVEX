package DavexAgent.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexAgent.module.auth.service.AuthService;
import DavexBase.common.My;
import DavexBase.entity.Agent;
import DavexBase.mapper.AgentMapper;

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
        if (my.getDavexType() != My.DavexType.DAVEX_AGENT) {
            throw new RuntimeException("DAVEX_AGENT启动失败: DAVEX_TYPE错误");
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
