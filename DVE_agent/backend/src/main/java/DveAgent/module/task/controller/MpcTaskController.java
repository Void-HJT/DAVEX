package DveAgent.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveAgent.module.task.service.MpcTaskService;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;


@RestController
@RequestMapping("/MpcTasks")
public class MpcTaskController {

    @Autowired
    private MpcTaskService MpcTaskService;

    @PostMapping("createMpcTask")
    public MpcTask createMpcTask(@RequestBody MpcTask mpcTask) {
        //需要修改一下 还要先填好
        return MpcTaskService.createMpcTask(mpcTask);
    }

    @PostMapping("/{mpcTaskId}/agents")
    //根据mpcTaskId将agents添加到MpcTaskAgent表中
    public void addAgentToMPCTask(@PathVariable Long mpcTaskId, @RequestBody List<MpcTaskAgent> agents) {
        MpcTaskService.addAgentToMPCTask(mpcTaskId, agents);
        //如果存成功了 就把这几个表发送给对应的agent 还得发mpcTask表
    }
}