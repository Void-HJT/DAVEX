package DavexCenter.module.task.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.common.Utils;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskOutput;
import DavexBase.info.UploadAgentTaskInfo;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.task.service.MpcTaskOutputService;
import DavexCenter.module.task.service.MpcTaskService;

@RestController
@RequestMapping("/MpcTasks")
public class MpcTaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @Autowired
    private My my;

    @Autowired
    private InputMapper inputMapper;

    @Autowired
    private MpcTaskOutputService mpcTaskOutputService;

    @PostMapping("/create")
    public R<MpcTask> createMpcTask(@RequestBody UploadAgentTaskInfo mpcTask) {
        try {
            mpcTask = mpcTaskService.create(mpcTask);
            switch (mpcTask.getTaskType()) {
                case GARNET_MPC:
                default:
                    mpcTaskService.mpcRun(mpcTask);
                    break;
                case GARNET_PSI:
                    mpcTaskService.psiRun(mpcTask);
                    break;
            }
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(mpcTask, "成功创建");
    }

    @PostMapping("/create_with_input")
    public R<MpcTask> postMethodName(@RequestPart("file") MultipartFile file,
            @RequestPart("mpcTask") UploadAgentTaskInfo mpcTask) {
        Path path = Utils.resolveFileNameConflict(
                Paths.get(my.getBase_path()).resolve("Input").resolve(file.getOriginalFilename()));
        Input input = new Input();
        input.setApplicationId(mpcTask.getApplicationId());
        input.setPath(Paths.get("Input").resolve(path.getFileName()).toString());
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            inputMapper.insert(input);
            mpcTask.setDataId(input.getUid());
            mpcTask = mpcTaskService.create(mpcTask);
            switch (mpcTask.getTaskType()) {
                case GARNET_MPC:
                default:
                    mpcTaskService.mpcRun(mpcTask);
                    break;
                case GARNET_PSI:
                    mpcTaskService.psiRun(mpcTask);
                    break;
            }
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(mpcTask, "成功创建");
    }

    @GetMapping("/ready")
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

    @PostMapping("/save")
    public R<?> saveOutput(@RequestPart("file") MultipartFile file,
            @RequestPart("metadata") MpcTaskOutput mpcTaskOutput) {
        try {
            mpcTaskOutputService.saveOutputFromAgent(file, mpcTaskOutput);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
        return R.success("保存成功");
    }

    @GetMapping("list")
    public List<MpcTask> list() {
        return mpcTaskService.list();
    }

}