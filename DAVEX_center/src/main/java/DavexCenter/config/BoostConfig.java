package DavexCenter.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.My;
import DavexBase.entity.Center;
import DavexBase.mapper.CenterMapper;
import DavexCenter.module.auth.service.AuthService;

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
        if (my.getDavexType() != My.DavexType.DAVEX_CENTER) {
            throw new RuntimeException("DAVEX_CENTER启动失败: DAVEX_TYPE错误");
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
