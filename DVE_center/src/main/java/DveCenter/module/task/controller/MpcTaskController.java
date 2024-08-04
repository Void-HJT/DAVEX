package DveCenter.module.task.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DveAgent.common.R;
import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskOutput;
import DveAgent.mapper.MpcTaskOutputMapper;
import DveCenter.common.My;
import DveCenter.info.UploadCenterTaskInfo;
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

    @PostMapping("/save")
    public R<?> save(@RequestPart("file") MultipartFile file, @RequestPart("metadata") MpcTaskOutput mpcTaskOutput) {
        if (mpcTaskOutputMapper.selectById(mpcTaskOutput.getUid()) != null) {
            return R.error("文件已保存");
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