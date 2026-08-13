package DavexCenter.module.task.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.task.port.AgentSecureInferenceClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 使用 WebClient 实现 Center 到 Agent 的安全推理通信。
 */
@Component
public class WebClientAgentSecureInferenceClient implements AgentSecureInferenceClient {

    private final CenterWebClientService webClientService;

    public WebClientAgentSecureInferenceClient(CenterWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public void createTasks(List<TaskDispatch> dispatches) throws Exception {

        List<Mono<Void>> requests = new ArrayList<>();

        for (TaskDispatch dispatch : dispatches) {
            Mono<Void> request = webClientService
                    .center2AgentWebClient(dispatch.agentId())
                    .post()
                    .uri("/SecureInference/create")
                    .bodyValue(dispatch.request())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<R<?>>() {
                    })
                    .flatMap(response -> {
                        if (response == null || response.getBody() == null) {
                            return Mono.error(new IllegalStateException(
                                    "Agent返回空的安全推理响应"));
                        }

                        if (response.getBody().getCode() == 0) {
                            return Mono.error(new IllegalStateException(
                                    response.getBody().getMessage()));
                        }

                        return Mono.empty();
                    });

            requests.add(request);
        }

        // 保留原有行为：多个 Agent 的创建请求并发执行。
        Mono.when(requests).block();
    }

    @Override
    public Artifact fetchArtifact(String agentId, String fileId) throws Exception {

        R<Mpc> response = webClientService
                .center2AgentWebClient(agentId)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/SecureInference/getMpc")
                        .queryParam("FileID", fileId)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<Mpc>>() {
                })
                .block();

        if (response == null
                || response.getBody() == null
                || response.getBody().getCode() == 0
                || response.getBody().getData() == null) {
            throw new Exception("Agent返回无效的推理MPC元数据");
        }

        Mpc mpc = response.getBody().getData();

        Resource resource = webClientService
                .center2AgentWebClient(agentId)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Mpc/download")
                        .queryParam("MpcID", mpc.getUid())
                        .build())
                .retrieve()
                .bodyToMono(Resource.class)
                .block();

        if (resource == null || resource.getFilename() == null) {
            throw new Exception("Agent返回空的推理程序文件");
        }

        // Spring Resource 到普通字节数据的转换只发生在通信适配器中。
        try (InputStream inputStream = resource.getInputStream()) {
            return new Artifact(
                    mpc,
                    resource.getFilename(),
                    inputStream.readAllBytes());
        }
    }
}