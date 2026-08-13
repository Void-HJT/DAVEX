package DavexAgent.module.task.port;

import DavexBase.entity.Mpc;

/**
 * 定义 Agent 从 Center 获取 MPC 程序元数据和程序文件的业务边界。
 */
public interface CenterMpcArtifactClient {

    Artifact fetchArtifact(String centerId, String mpcId) throws Exception;

    /**
     * 封装远端 MPC 元数据、文件名和文件内容，避免业务层依赖 Resource。
     */
    record Artifact(Mpc metadata, String fileName, byte[] content) {
    }
}