package DavexCenter.module.task.port;

import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

/**
 * Center 调用 Agent MPC 任务接口的业务端口。
 *
 * 上层业务只描述创建、就绪检查和启动操作，不感知 WebClient 和远程 URI。
 */
public interface AgentMpcTaskClient {

    void createTask(String agentId, MpcTaskCreateRequest request)
            throws Exception;

    boolean isReady(String agentId, String mpcTaskId)
            throws Exception;

    boolean runTask(String agentId, String mpcTaskId)
            throws Exception;
}
