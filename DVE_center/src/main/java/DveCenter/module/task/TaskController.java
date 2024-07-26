package DveCenter.module.task;

import DveCenter.common.My;
import DveCenter.module.auth.service.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import DveAgent.common.R;
import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveCenter.info.UploadCenterTaskInfo;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/MpcTasks")
public class TaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @Autowired
    CenterWebClientService centerWebClientService;

    @Autowired
    My my;

    // TODO 直接将Input一并指定
    @PostMapping("/create")
    public R<MpcTask> createMpcTask(@RequestBody UploadCenterTaskInfo mpcTask) {
        try {
            mpcTaskService.create(mpcTask);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(mpcTask, "成功创建");
    }

    @GetMapping("{mpcTaskId}/ready")
    public R<?> checkTaskStatus(@PathVariable Long mpcTaskId) {
        try {
            if (mpcTaskService.ready(mpcTaskId)) {
                return R.success("就绪");
            }
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.error("未就绪");
    }

    // @PostMapping("{mpcTaskId}/execute")
    // public void runTask(@PathVariable Long mpctaskId) {
    // // 根据taskId 和 centerId查task agent表 确认需要发的agent有哪几个
    // long centerId = my.getId();
    // List<MpcTaskAgent> agents =
    // mpcTaskService.getAgentsByMpcTaskIdAndCenterId(mpctaskId, centerId);

    // // 给每个agent发check信息
    // try {
    // for (MpcTaskAgent agent : agents) {
    // if (checkTaskStatus(agent.getAgentId(), mpctaskId).getStatus() != "ready") {
    // // 如果有任意一方没有准备就绪 结束并返回错误
    // }
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // // check无误后执行本地任务
    // MpcTask mpcTask = mpcTaskService.getTaskByMpcTaskIdAndCenterId(mpctaskId,
    // centerId);

    // // 并给agent发请求也执行他们的任务
    // try {
    // for (MpcTaskAgent agent : agents) {
    // WebClient client = centerWebClientService.center2AgentWebClient((int)
    // agent.getAgentId());
    // // 需要mpcTaskId和centerId唯一确认一个执行的任务
    // long mpcTaskIdLong = mpctaskId.longValue();
    // client.post().uri("/MpcTasks/" + mpcTaskIdLong +
    // "/execute").bodyValue(centerId).retrieve()
    // .bodyToMono(void.class).block();
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
}