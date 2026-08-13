package DavexCenter.module.task.port;

import java.util.List;

import DavexBase.entity.Mpc;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

/**
 * 定义 Center 向 Agent 创建安全推理任务和获取推理程序的通信边界。
 */
public interface AgentSecureInferenceClient {

    void createTasks(List<TaskDispatch> dispatches) throws Exception;

    Artifact fetchArtifact(String agentId, String fileId) throws Exception;

    /**
     * 表示需要发送给某个 Agent 的安全推理任务。
     */
    record TaskDispatch(String agentId, MpcTaskCreateRequest request) {
    }

    /**
     * 封装远端推理程序的元数据、文件名和文件内容。
     */
    record Artifact(Mpc metadata,String fileName,byte[] content) {
    }
}
