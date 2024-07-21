package DveCenter.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveCenter.common.My;
import DveAgent.common.Utlis;
import DveAgent.entity.Center;
import DveAgent.mapper.CenterMapper;

@Configuration
public class BoostConfig {
    @Autowired
    My my;

    @Autowired
    private CenterMapper centerMapper;

    @Autowired
    DveCenter.module.auth.service.SSLContextService sslContextService;

    @Bean
    void boost() throws Exception {
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, my.getId());
        Center center = centerMapper.selectOne(queryWrapper);
        if (center == null) {
            centerMapper.insert(my.getCenter());
            return;
        } else if (!my.getCenter().equals(center)) {
            centerMapper.update(my.getCenter(), queryWrapper);
        }

        LambdaQueryWrapper<Center> agentListQuery = Wrappers.<Center>lambdaQuery();
        List<Center> agents = centerMapper.selectList(agentListQuery);
        for (Center a : agents) {
            if (my.getTrustStore().containsAlias(a.getName())) {
                my.getTrustStore().deleteEntry(a.getName());
            }
            my.getTrustStore().setCertificateEntry(a.getName(), Utlis.bytesToCertificate(a.getCrt()));
        }
        sslContextService.configureGlobalSSLContext(my.getTrustStore(), my.getKeyStore());
    }

}
