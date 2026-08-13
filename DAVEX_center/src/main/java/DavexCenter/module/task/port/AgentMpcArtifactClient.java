package DavexCenter.module.task.port;

import DavexBase.entity.Mpc;

/**
 * 定义 Center 从 Agent 获取 MPC 程序元数据和程序文件的业务边界。
 */
public interface AgentMpcArtifactClient {

    Artifact fetchArtifact(String agentId, String mpcId) throws Exception;

    /**
     * 封装远端 MPC 元数据、文件名和文件内容，避免业务层依赖 Resource。
     */
    record Artifact(Mpc metadata, String fileName, byte[] content) {
    }
}
