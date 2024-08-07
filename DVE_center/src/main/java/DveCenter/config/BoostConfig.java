package DveCenter.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveBase.common.My;
import DveBase.entity.Center;
import DveBase.mapper.CenterMapper;
import DveCenter.module.auth.service.AuthService;

@Configuration
public class BoostConfig {

    @Autowired
    private My my;

    @Autowired
    private CenterMapper centerMapper;

    @Autowired
    private AuthService authService;

    @PostConstruct
    private void boost() {
        if (my.getDveType() != My.DveType.DVE_CENTER) {
            throw new RuntimeException("DVE_CENTER启动失败: DVE_TYPE错误");
        }
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, my.getId());
        Center old_center = centerMapper.selectOne(queryWrapper);
        Center new_center = (Center) my.getMyObject();
        if (old_center == null) {
            centerMapper.insert(new_center);
            authService.broacast(new_center);

        } else if (!old_center.equals(new_center)) {
            centerMapper.updateById(new_center);
            authService.broacast(new_center);
        }

    }

}
