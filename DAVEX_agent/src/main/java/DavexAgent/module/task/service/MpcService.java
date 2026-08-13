package DavexAgent.module.task.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import DavexAgent.module.task.port.CenterMpcArtifactClient;
import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.entity.Mpc;
import DavexBase.mapper.MpcMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 负责将远端 MPC 程序包保存到 Agent 本地并登记其存储路径。
 */
@Service
public class MpcService {

    @Autowired
    private CenterMpcArtifactClient artifactClient;

    @Autowired
    private My my;

    @Autowired
    private MpcMapper mpcMapper;

    private static final Logger logger = LoggerFactory.getLogger(MpcService.class);

    public void downloadMPC(String centerId, String mpcId) throws Exception {

        Mpc existing = mpcMapper.selectById(mpcId);

        CenterMpcArtifactClient.Artifact artifact = artifactClient.fetchArtifact(centerId, mpcId);

        // 业务层只负责本地落盘，不再处理 HTTP 和 Resource。
        Path programsDirectory = Paths.get(my.getBase_path()).resolve("programs");
        Files.createDirectories(programsDirectory);

        Path filePath = Utils.resolveFileNameConflict(programsDirectory.resolve(artifact.fileName()));
        Files.write(filePath, artifact.content());

        Mpc mpc = artifact.metadata();
        mpc.setPath(Paths.get("programs")
                .resolve(filePath.getFileName())
                .toString());
        // 允许修复“数据库有元数据、磁盘无程序文件”的不一致状态。
        if (existing == null) {
            mpcMapper.insert(mpc);
        } else {
            mpcMapper.updateById(mpc);
        }

        logger.info("成功下载{} : {}", mpc.getUid(), mpc.getName());
    }
}