package DveCenter.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveBase.entity.Center;
import DveBase.mapper.CenterMapper;
import DveCenter.common.MyCenter;
import DveCenter.module.auth.service.AuthService;

@Configuration
public class BoostConfig {

    @Autowired
    private MyCenter my;

    @Autowired
    private CenterMapper centerMapper;

    @Autowired
    private AuthService authService;

    @PostConstruct
    private void boost() {
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, my.getId());
        Center old_center = centerMapper.selectOne(queryWrapper);
        if (old_center == null) {
            centerMapper.insert(my.getCenter());
            authService.broacast(my.getCenter());

        } else if (!old_center.equals(my.getCenter())) {
            centerMapper.updateById(my.getCenter());
            authService.broacast(my.getCenter());
        } else {
            my.setCenter(old_center);
        }

    }

}
