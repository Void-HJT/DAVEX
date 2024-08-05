package DveAgent.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DveAgent.common.MyAgent;
import DveAgent.module.task.service.GarnetService;
import DveAgent.module.task.service.MpcTaskService;
import DveBase.common.R;
import DveBase.entity.MpcTask;
import DveBase.info.UploadAgentTaskInfo;
import DveBase.mapper.MpcTaskMapper;

@RestController
@RequestMapping("/MpcTasks")
public class MpcTaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @Autowired
    MyAgent my;

    @Autowired
    GarnetService garnetService;

    @Autowired
    MpcTaskMapper mpcTaskMapper;

    @PostMapping("create")
    public R<?> createMpcTask(@RequestBody UploadAgentTaskInfo mpcTask) {
        try {
            mpcTaskService.createMpcTask(mpcTask);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success("成功创建");
    }

    @GetMapping("/ready")
    public R<Boolean> ready(@RequestParam String mpcTaskId) {
        try {
            return R.success(mpcTaskService.ready(mpcTaskId), "");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/run")
    public R<Boolean> run(@RequestParam String mpcTaskId) {
        MpcTask mpcTask = mpcTaskMapper.selectById(mpcTaskId);
        if (mpcTask == null) {
            return R.error("任务不存在");
        }
        switch (mpcTask.getStatus()) {
            case READY:
                break;

            case COMPILING:
                return R.success(false, "任务正在编译");
            case RUNNING:
                return R.error("任务已在运行中");
            case FAILED:
                return R.error("任务失败");
            case FINISHED:
                return R.error("任务已完成");
            default:
                return R.error("错误");
        }
        try {
            switch (mpcTask.getTaskType()) {
                case GARNET_PSI:
                    mpcTaskService.psiRun(mpcTask);
                    break;
                case GARNET_MPC:
                default:
                    mpcTaskService.run(mpcTask);
                    break;
            }

        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(true, "任务正在运行");

    }

}