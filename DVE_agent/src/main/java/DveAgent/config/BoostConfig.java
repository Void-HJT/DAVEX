package DveAgent.config;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.My;
import DveAgent.common.Utlis;
import DveAgent.entity.Agent;
import DveAgent.entity.Center;
import DveAgent.mapper.AgentMapper;
import DveAgent.mapper.CenterMapper;

@Configuration
public class BoostConfig {
    @Autowired
    My my;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private CenterMapper centerMapper;        

    @Autowired
    DveAgent.module.auth.service.SSLContextService sslContextService;

    @PostConstruct
    void boost() throws Exception {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, my.getId());
        Agent agent = agentMapper.selectOne(queryWrapper);
        if (agent == null) {
            agentMapper.insert(my.getAgent());
            return;
        } else if (!my.getAgent().equals(agent)) {
            agentMapper.update(my.getAgent(), queryWrapper);
        }

        LambdaQueryWrapper<Center> centerListQuery = Wrappers.<Center>lambdaQuery();
        List<Center> centers = centerMapper.selectList(centerListQuery);
        for (Center c : centers) {
            if (my.getTrustStore().containsAlias(c.getName())) {
                my.getTrustStore().deleteEntry(c.getName());
            }
            my.getTrustStore().setCertificateEntry(c.getName(), Utlis.bytesToCertificate(c.getCrt()));
        }
        sslContextService.configureGlobalSSLContext(my.getTrustStore(), my.getKeyStore());
    }

}
