package DavexCenter.module.task.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.entity.Mpc;
import DavexBase.mapper.MpcMapper;
import DavexCenter.module.task.port.AgentMpcArtifactClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 负责将远端 MPC 程序包保存到 Center 本地并登记其存储路径。
 */
@Service
@ConditionalOnProperty(name = "garnet.enabled", havingValue = "true")
public class MpcService {

    @Autowired
    private AgentMpcArtifactClient artifactClient;

    @Autowired
    private My my;

    @Autowired
    private MpcMapper mpcMapper;

    private static final Logger logger = LoggerFactory.getLogger(MpcService.class);

    public void downloadMPC(String agentId, String mpcId) throws Exception {

        AgentMpcArtifactClient.Artifact artifact = artifactClient.fetchArtifact(agentId, mpcId);

        // 业务层只负责本地落盘，不再处理 HTTP 和 Resource。
        Path programsDirectory = Paths.get(my.getBase_path()).resolve("programs");
        Files.createDirectories(programsDirectory);

        Path filePath = Utils.resolveFileNameConflict(programsDirectory.resolve(artifact.fileName()));
        Files.write(filePath, artifact.content());

        Mpc mpc = artifact.metadata();
        mpc.setPath(Paths.get("programs").resolve(filePath.getFileName()).toString());
        mpcMapper.insert(mpc);

        logger.info("成功下载{} : {}", mpc.getUid(), mpc.getName());
    }
}