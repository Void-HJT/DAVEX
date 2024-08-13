package DavexCenter.module.auth.controller;

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

import DavexBase.common.R;
import DavexBase.entity.Agent;
import DavexBase.mapper.AgentMapper;

@RestController
@RequestMapping("/agents")
public class AgentController {
    @Autowired
    AgentMapper agentMapper;

    @GetMapping("/list")
    public R<List<Agent>> list() {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery();
        return R.success(agentMapper.selectList(queryWrapper), "查询成功");
    }

    @PostMapping("/update")
    public R<?> update(@RequestBody Agent agent) {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent.getUid());
        if (agentMapper.selectOne(queryWrapper) != null) {
            try {
                agentMapper.updateById(agent);
                return R.success("更新成功");
            } catch (Exception ex) {
                return R.error(ex.getMessage());
            }
        } else {
            try {
                agentMapper.insert(agent);
                return R.success("更新成功");
            } catch (Exception ex) {
                return R.error(ex.getMessage());
            }
        }

    }

    @PostMapping("/insert")
    public R<?> insert(@RequestBody Agent agent) {
        try {
            agentMapper.insert(agent);
            return R.success("插入成功");
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }

    }

    @DeleteMapping("/delete")
    public R<?> deletet(@RequestBody Agent agent) {
        try {
            agentMapper.deleteById(agent.getUid());
            return R.success("插入成功");
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }

    }
}
