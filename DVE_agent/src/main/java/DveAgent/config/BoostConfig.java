package DveAgent.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.My;
import DveAgent.common.Utlis;
import DveAgent.entity.Agent;
import DveAgent.mapper.AgentMapper;

@Configuration
public class BoostConfig {
    @Autowired
    My my;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    DveAgent.module.auth.service.SSLContextService sslContextService;

    @Bean
    void boost() throws Exception {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, my.getId());
        Agent agent = agentMapper.selectOne(queryWrapper);
        if (agent == null) {
            agentMapper.insert(my.getAgent());
            return;
        } else if (!my.getAgent().equals(agent)) {
            agentMapper.update(my.getAgent(), queryWrapper);
        }

        LambdaQueryWrapper<Agent> agentListQuery = Wrappers.<Agent>lambdaQuery();
        List<Agent> agents = agentMapper.selectList(agentListQuery);
        for (Agent a : agents) {
            if (my.getTrustStore().containsAlias(a.getName())) {
                my.getTrustStore().deleteEntry(a.getName());
            }
            my.getTrustStore().setCertificateEntry(a.getName(), Utlis.bytesToCertificate(a.getCrt()));
        }
        sslContextService.configureGlobalSSLContext(my.getTrustStore(), my.getKeyStore());
    }

}
