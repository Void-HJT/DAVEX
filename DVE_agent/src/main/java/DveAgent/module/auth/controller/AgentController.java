package DveAgent.module.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import DveAgent.common.R;
import DveAgent.entity.Center;
import DveAgent.mapper.CenterMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/centers")
public class AgentController {

    @Autowired
    private CenterMapper centerMapper;

    @PostMapping("/update")
    public R<?> update(@RequestBody Center center) {
        centerMapper.updateById(center);
        return R.success("成功");
    }

}
