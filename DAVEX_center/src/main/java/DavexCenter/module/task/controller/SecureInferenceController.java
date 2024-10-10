package DavexCenter.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.R;
import DavexBase.entity.MpcTask;
import DavexBase.info.InferenceInfo;
import DavexBase.info.UploadAgentTaskInfo;
import DavexCenter.module.task.service.SecureInferenceService;

@RestController
@RequestMapping("/SecureInference")
public class SecureInferenceController {

    @Autowired
    SecureInferenceService secureInferenceService;

    @PostMapping("/create")
    public R<MpcTask> postMethodName(@RequestPart("file") MultipartFile file,
            @RequestPart("mpcTask") InferenceInfo InferenceInfo) {
        try {
            UploadAgentTaskInfo mpcTask = secureInferenceService.wrapMpcTaskInfo(InferenceInfo);
            mpcTask = secureInferenceService.create(mpcTask);
            secureInferenceService.run(mpcTask);
            return R.success(mpcTask, "成功创建");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
