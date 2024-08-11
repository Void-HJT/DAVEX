package DveCenter.module.task.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import DveBase.common.My;
import DveBase.common.Utils;
import DveBase.entity.MpcTask;
import DveBase.entity.MpcTaskOutput;
import DveBase.mapper.MpcTaskOutputMapper;

@Service
public class MpcTaskOutputService {

    @Autowired
    MpcTaskOutputMapper mpcTaskOutputMapper;

    @Autowired
    My my;

    public void saveOutputFromAgent(MultipartFile file, MpcTaskOutput mpcTaskOutput) throws Exception {
        if (mpcTaskOutputMapper.selectById(mpcTaskOutput.getUid()) != null) {
            throw new Exception("文件已保存");
        }
        if (!Utils.verifyFileHash((FileSystemResource) file, mpcTaskOutput.getHash(), "SHA-256")) {
            throw new Exception("文件hash不匹配");
        }
        String fileName = file.getOriginalFilename();
        Path path = Paths.get(my.getBase_path()).resolve("mpctask").resolve(fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw e;
        }
        mpcTaskOutputMapper.insert(mpcTaskOutput);
    }

    public void saveOutputFromInner(MpcTask mpcTask) throws Exception {
        MpcTaskOutput mpcTaskOutput = new MpcTaskOutput();
        Path outputPath = Paths.get(my.getGarnet_path()).resolve("Output")
                .resolve(mpcTask.getUid() + "-P" + mpcTask.getPart() + "-0");
        Path savePath = Paths.get(my.getBase_path()).resolve("mpctask").resolve(mpcTask.getUid());
        mpcTaskOutput.setPath(Paths.get("mpctask").resolve(mpcTask.getUid()).toString());
        mpcTaskOutput.setTaskId(mpcTask.getUid());
        try {
            mpcTaskOutput.setHash(Utils.getFileHash(new FileSystemResource(outputPath), "SHA-256"));
            Files.move(outputPath, savePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw e;
        }
        mpcTaskOutputMapper.insert(mpcTaskOutput);
    }
}
