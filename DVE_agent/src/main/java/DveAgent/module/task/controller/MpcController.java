package DveAgent.module.task.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.MyAgent;
import DveBase.common.R;
import DveBase.entity.Mpc;
import DveBase.mapper.MpcMapper;

@RestController
@RequestMapping("/Mpc")
public class MpcController {
    @Autowired
    MyAgent my;

    @Autowired
    MpcMapper mpcMapper;

    @GetMapping("/list")
    public R<List<Mpc>> list() {
        LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery();
        return R.success(mpcMapper.selectList(queryWrapper), "查询成功");
    }

    @GetMapping("/select")
    public R<Mpc> select(@RequestParam Long MpcID) {
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
