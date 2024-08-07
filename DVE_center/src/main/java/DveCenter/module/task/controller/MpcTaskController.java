package DveCenter.module.task.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DveBase.common.My;
import DveBase.common.R;
import DveBase.common.Utils;
import DveBase.entity.MpcTask;
import DveBase.entity.MpcTaskOutput;
import DveBase.info.UploadAgentTaskInfo;
import DveBase.mapper.MpcTaskOutputMapper;
import DveCenter.entity.Input;
import DveCenter.mapper.InputMapper;
import DveCenter.module.auth.service.CenterWebClientService;
import DveCenter.module.task.service.MpcTaskService;

@RestController
@RequestMapping("/MpcTasks")
public class MpcTaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @Autowired
    CenterWebClientService centerWebClientService;

    @Autowired
    My my;

    @Autowired
    MpcTaskOutputMapper mpcTaskOutputMapper;

    @Autowired
    InputMapper inputMapper;

    @PostMapping("/create")
    public R<MpcTask> createMpcTask(@RequestBody UploadAgentTaskInfo mpcTask) {
        try {
            mpcTaskService.create(mpcTask);
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
            mpcTaskService.create(mpcTask);
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
    public R<?> save(@RequestPart("file") MultipartFile file, @RequestPart("metadata") MpcTaskOutput mpcTaskOutput) {
        if (mpcTaskOutputMapper.selectById(mpcTaskOutput.getUid()) != null) {
            return R.error("文件已保存");
        }
        if (!Utils.verifyFileHash((FileSystemResource) file, mpcTaskOutput.getHash(), "SHA-256")) {
            return R.error("文件Hash不匹配");
        }
        String fileName = file.getOriginalFilename();
        Path path = Paths.get(my.getBase_path()).resolve("mpctask").resolve(fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            return R.error(e.getMessage());
        }
        mpcTaskOutputMapper.insert(mpcTaskOutput);
        return R.success("保存成功");
    }
}