package DveAgent.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import DveAgent.common.My;
import DveAgent.common.R;
import DveAgent.info.UploadAgentTaskInfo;
import DveAgent.module.task.service.MpcTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/MpcTasks")
public class MpcTaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @Autowired
    My my;

    @PostMapping("create")
    public R<?> createMpcTask(@RequestBody UploadAgentTaskInfo mpcTask) {
        try {
            mpcTaskService.createMpcTask(mpcTask);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success("成功创建");
    }

    @GetMapping("/ready/{mpcTaskId}")
    public R<Boolean> getMethodName(@RequestParam Long mpcTaskId) {
        try {
            return R.success(mpcTaskService.ready(mpcTaskId), "");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}