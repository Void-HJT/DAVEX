package DavexCenter.module.task.adapter;

import DavexBase.common.R;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.task.port.AgentMpcTaskClient;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * 使用现有 WebClient 工厂实现 Center 到 Agent 的 MPC 任务通信。
 *
 * 远程地址、URI、响应解析和错误语义被限制在该适配器内，
 * MPC 业务 Service 不再直接依赖 WebClient。
 */
@Component
public class WebClientAgentMpcTaskClient
        implements AgentMpcTaskClient {

    private final CenterWebClientService webClientService;

    public WebClientAgentMpcTaskClient(
            CenterWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public void createTask(String agentId, MpcTaskCreateRequest request) throws Exception {
        R<?> response = webClientService
                .center2AgentWebClient(agentId)
                .post()
                .uri("/MpcTasks/create")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<?>>() {
                })
                .block();

        requireSuccessfulResponse(response);
    }

    @Override
    public boolean isReady(String agentId, String mpcTaskId) throws Exception {

        return invokeBooleanOperation(agentId, "/MpcTasks/ready", mpcTaskId);
    }

    @Override
    public boolean runTask(String agentId, String mpcTaskId) throws Exception {

        return invokeBooleanOperation(agentId, "/MpcTasks/run", mpcTaskId);
    }

    private boolean invokeBooleanOperation(String agentId, String path, String mpcTaskId) throws Exception {

        R<Boolean> response = webClientService
                .center2AgentWebClient(agentId)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("mpcTaskId", mpcTaskId)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<Boolean>>() {
                })
                .block();

        requireSuccessfulResponse(response);
        return Boolean.TRUE.equals(response.getBody().getData());
    }

    private void requireSuccessfulResponse(R<?> response)
            throws Exception {

        if (response == null || response.getBody() == null) {
            throw new Exception("Agent返回空响应");
        }

        if (response.getBody().getCode() == 0) {
            throw new Exception(response.getBody().getMessage());
        }
    }
}
