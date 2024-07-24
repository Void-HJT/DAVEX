package DveCenter.config;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveCenter.common.My;
import DveAgent.common.Utlis;
import DveAgent.entity.Agent;
import DveAgent.entity.Center;
import DveAgent.entity.Application;
import DveAgent.mapper.AgentMapper;
import DveAgent.mapper.ApplicationMapper;
import DveAgent.mapper.CenterMapper;

@Configuration
public class BoostConfig {
    @Autowired
    My my;

    @Autowired
    private CenterMapper centerMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    DveCenter.module.auth.service.SSLContextService sslContextService;

    @Autowired
    DveCenter.module.auth.service.AuthService authService;

    @PostConstruct
    void boost() throws Exception {
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, my.getId());
        Center center = centerMapper.selectOne(queryWrapper);
        if (center == null) {
            // my.getCenter().setLastUpdated(System.currentTimeMillis());
            centerMapper.insert(my.getCenter());
            // authService.broadcast(my.getCenter());
        } else if (!my.getCenter().equals(center)) {
            centerMapper.update(my.getCenter(), queryWrapper);
            // authService.broadcast(my.getCenter());
        }

        LambdaQueryWrapper<Agent> agentListQuery = Wrappers.<Agent>lambdaQuery();
        List<Agent> agents = agentMapper.selectList(agentListQuery);
        for (Agent a : agents) {
            if (my.getTrustStore().containsAlias(a.getName())) {
                my.getTrustStore().deleteEntry(a.getName());
            }
            my.getTrustStore().setCertificateEntry(a.getName(), Utlis.bytesToCertificate(a.getCrt()));
        }

        LambdaQueryWrapper<Application> applicationListQuery = Wrappers.<Application>lambdaQuery();
        List<Application> applications = applicationMapper.selectList(applicationListQuery);
        for (Application a : applications) {
            if (my.getTrustStore().containsAlias(a.getName())) {
                my.getTrustStore().deleteEntry(a.getName());
            }
            my.getTrustStore().setCertificateEntry(a.getName(), Utlis.bytesToCertificate(a.getCrt()));
        }
        sslContextService.configureGlobalSSLContext(my.getTrustStore(), my.getKeyStore());
    }

}
