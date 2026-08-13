package DavexCenter.module.task.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.common.Utils;
import DavexBase.entity.MpcTask;
import DavexBase.info.InferenceInfo;
import DavexBase.task.command.MpcTaskCommand;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.task.service.SecureInferenceService;

@RestController
@RequestMapping("/SecureInference")
@ConditionalOnProperty(name = "garnet.enabled", havingValue = "true")
public class SecureInferenceController {

    @Autowired
    SecureInferenceService secureInferenceService;
    @Autowired
    private My my;
    @Autowired
    private InputMapper inputMapper;

    @PostMapping("/create")
    public R<MpcTask> create(@RequestPart("file") MultipartFile file,
            @RequestPart("inferenceInfo") InferenceInfo inferenceInfo) {
        Path path = Utils.resolveFileNameConflict(
                Paths.get(my.getBase_path()).resolve("Input").resolve(file.getOriginalFilename()));
        Input input = new Input();
        input.setApplicationId(inferenceInfo.getApplicationId());
        input.setPath(Paths.get("Input").resolve(path.getFileName()).toString());
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            inputMapper.insert(input);
            MpcTaskCommand command =
                    secureInferenceService.wrapMpcTaskCommand(
                            inferenceInfo,
                            input.getUid());
            MpcTask created = secureInferenceService.create(command);
            secureInferenceService.run(created);
            return R.success(created, "成功创建");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
