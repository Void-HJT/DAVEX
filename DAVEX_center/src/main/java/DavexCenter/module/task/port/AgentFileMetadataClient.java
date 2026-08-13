package DavexCenter.module.task.port;

/**
 * 查询 Agent 侧 MPC 输入文件元数据的业务端口。
 *
 * 文件通信与任务通信分开，避免 AgentMpcTaskClient 承担文件职责。
 */
public interface AgentFileMetadataClient {

    long getRowCount(String agentId, String fileId)
            throws Exception;
}
