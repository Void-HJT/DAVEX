package DveAgent.module.task.controller;

import DveAgent.common.My;
import DveAgent.entity.Mpc;
import org.apache.ibatis.annotations.Param;
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
    private MpcTaskService mpcTaskService;

    @Autowired
    My my;

    @PostMapping("createMpcTask")
    public MpcTask createMpcTask(@RequestBody MpcTask mpcTask) {
        return mpcTaskService.createMpcTask(mpcTask);
    }

    @PostMapping("/{mpcTaskId}/agents")
    //根据mpcTaskId将agents添加到MpcTaskAgent表中
    //是否需要返回数据后面再考虑吧
    public void addAgentToMPCTask(@PathVariable Long mpcTaskId, @RequestBody List<MpcTaskAgent> agents) {
        mpcTaskService.addAgentToMPCTask(mpcTaskId, agents);
        //如果存成功了 就把这几个表发送给对应的agent 还得发mpcTask表 然后agent接到以后去填自己本地的表项（这一步要怎么做？）
    }

    @PostMapping("{mpcTaskId}/execute")
    public void runTask(@PathVariable Long mpcTaskId , @RequestBody Long centerId){
        //根据mpcTaskId 和 centerId 找到对应的mpcTask，并且执行
        MpcTask mpcTask = mpcTaskService.getTaskByMpcTaskIdAndCenterId(mpcTaskId,centerId);
    }

    @PostMapping("{mpcTaskId}/receiveAgentTable")
    public void receiveAgentTable(@PathVariable Long mpcTaskId,@RequestBody List<MpcTaskAgent> agents){
        mpcTaskService.addAgentToMPCTask(mpcTaskId, agents);
    }

    @PostMapping("{mpcTaskId}/receiveMpcTaskTable")
    public MpcTask receiveMpcTaskTable(@PathVariable Long mpcTaskId,@RequestBody MpcTask mpcTask){
        long agentId = my.getId();
        //根据接收到的table填好自己的MpcTaskTable
        //看看字段
        /*
        private long uid;
        private long applicationId; 用不到
        private long centerId;
        private long mpcId;
        private String parameter;
        private long pn; 这个要改
        private String host;
        private long port;
        private long data; 这个也要改
        private String protocol;
        */
        //如何查出对应的pn 去mpcTaskAgent表中查找 mpcTaskId 和 agentId都符合的那个pn 问题来了agentId从哪里拿？ 从my中 这个逻辑完成了
        long pn = mpcTaskService.getPartByMpcTaskIdAndAgentId(mpcTaskId,agentId).getPart();
        mpcTask.setPn(pn);
        mpcTaskService.createMpcTask(mpcTask);
        return mpcTask;
        //还留了一个bug 这个必须是唯一的 但有个问题，还得centerId 才行 因为不同的center会撞 晚点修好了
        //保持数据表项的唯一性

    }

    @PostMapping("{mpcTaskId}/checkTaskStatus")
    public MpcTask checkTaskStatus(@PathVariable Long mpcTaskId, @RequestBody Long centerId){
        //根据mpcTaskId和centerId查询对应的mpcTask
        return mpcTaskService.getTaskByMpcTaskIdAndCenterId(mpcTaskId,centerId);
    }
}