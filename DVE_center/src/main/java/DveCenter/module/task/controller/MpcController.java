package DveCenter.module.task.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveCenter.common.My;
import DveAgent.common.R;
import DveAgent.entity.Mpc;
import DveAgent.mapper.MpcMapper;

@RestController
@RequestMapping("/Mpc")
public class MpcController {
    @Autowired
    My my;

    @Autowired
    MpcMapper mpcMapper;

    @GetMapping("/list")
    public R<List<Mpc>> list() {
        LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery();
        return R.success(mpcMapper.selectList(queryWrapper), "查询成功");
    }

    @GetMapping("/select/{MpcID}")
    public R<Mpc> select(@RequestBody Long MpcID) {
        LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery().eq(Mpc::getUid, MpcID);
        return R.success(mpcMapper.selectOne(queryWrapper), "查询成功");
    }

    @PostMapping("/create")
    public R<Mpc> create(@RequestBody Mpc mpc) {
        try {
            mpcMapper.insert(mpc);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(mpc, "创建成功");
    }

}
