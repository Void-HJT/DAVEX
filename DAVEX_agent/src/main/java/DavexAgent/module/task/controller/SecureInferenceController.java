package DavexAgent.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexAgent.module.task.service.SecureInferenceService;
import DavexBase.common.Body;
import DavexBase.common.R;
import DavexBase.entity.File;
import DavexBase.entity.Mpc;
import DavexBase.info.UploadAgentTaskInfo;
import DavexBase.mapper.FileMapper;

@RestController
@RequestMapping("/SecureInference")
public class SecureInferenceController {
    @Autowired
    private SecureInferenceService secureInferenceService;
    @Autowired
    private FileMapper fileMapper;

    @GetMapping("/getMpc")
    public R<Mpc> getMpc(@RequestParam String FileID) {
        File file = fileMapper.selectById(FileID);
        if (file == null) {
            return R.error("File not found");
        }
        if (!file.getType().toLowerCase().contains("secureinfer")) {
            return R.error("File type not supported");
        }
        try {
            return R.success(secureInferenceService.getMpc(file), "MpcID");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/setMpc")
    public Body<String> setMpc(@RequestParam String FileID, @RequestParam String mpcID) {
        File file = fileMapper.selectById(FileID);
        if (file == null) {
            return Body.error("File not found");
        }
        if (!file.getType().toLowerCase().contains("secureinfer")) {
            return Body.error("File type not supported");
        }
        return secureInferenceService.setMpc(file, mpcID);
    }

    @PostMapping("/create")
    public R<?> create(@RequestBody UploadAgentTaskInfo mpcTask) {
        try {
            secureInferenceService.create(mpcTask);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success("成功创建");
    }

}
