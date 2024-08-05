package DveCenter.module.auth.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveBase.common.R;
import DveBase.entity.Center;
import DveBase.mapper.CenterMapper;

@RestController
@RequestMapping("/centers")
public class CenterController {

    @Autowired
    private CenterMapper centerMapper;

    @GetMapping("/list")
    public R<List<Center>> list() {
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery();
        return R.success(centerMapper.selectList(queryWrapper), "查询成功");
    }

    @PostMapping("/update")
    public R<?> update(@RequestBody Center center) {
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, center.getUid());
        if (centerMapper.selectOne(queryWrapper) != null) {
            try {
                centerMapper.updateById(center);
                return R.success("更新成功");
            } catch (Exception ex) {
                return R.error(ex.getMessage());
            }
        } else {
            try {
                centerMapper.insert(center);
                return R.success("更新成功");
            } catch (Exception ex) {
                return R.error(ex.getMessage());
            }
        }

    }

    @PostMapping("/insert")
    public R<?> insert(@RequestBody Center center) {
        try {
            centerMapper.insert(center);
            return R.success("插入成功");
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }

    }

    @DeleteMapping("/delete")
    public R<?> deletet(@RequestBody Center center) {
        try {
            centerMapper.deleteById(center.getUid());
            return R.success("插入成功");
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }

    }

}
