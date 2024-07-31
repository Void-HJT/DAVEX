package DveCenter.module.task.controller;

import DveCenter.common.My;
import DveCenter.module.auth.service.CenterWebClientService;
import DveCenter.module.task.service.MpcTaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import DveAgent.common.R;
import DveAgent.entity.MpcTask;
import DveCenter.info.UploadCenterTaskInfo;

@RestController
@RequestMapping("/MpcTasks")
public class MpcTaskController {

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

    @GetMapping("/ready/{mpcTaskId}")
    public R<?> checkTaskStatus(@PathVariable String mpcTaskId) {
        try {
            if (mpcTaskService.ready(mpcTaskId)) {
                return R.success("就绪");
            }
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.error("未就绪");
    }
}