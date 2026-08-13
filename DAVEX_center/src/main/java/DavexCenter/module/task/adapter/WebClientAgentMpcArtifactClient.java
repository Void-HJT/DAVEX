package DavexCenter.module.task.adapter;

import java.io.InputStream;

import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.task.port.AgentMpcArtifactClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 通过 HTTP 从 Agent 查询并下载 MPC 程序包。
 */
@Component
public class WebClientAgentMpcArtifactClient implements AgentMpcArtifactClient {

    private final CenterWebClientService webClientService;

    public WebClientAgentMpcArtifactClient(CenterWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public Artifact fetchArtifact(String agentId, String mpcId) throws Exception {

        WebClient webClient = webClientService.center2AgentWebClient(agentId);

        R<Mpc> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Mpc/select")
                        .queryParam("MpcID", mpcId)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<Mpc>>() {
                })
                .block();

        if (response == null || response.getBody() == null || response.getBody().getCode() == 0
                || response.getBody().getData() == null) {
            throw new Exception("Agent返回无效的MPC元数据");
        }

        Resource resource = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Mpc/download")
                        .queryParam("MpcID", mpcId)
                        .build())
                .retrieve()
                .bodyToMono(Resource.class)
                .block();

        if (resource == null || resource.getFilename() == null) {
            throw new Exception("Agent返回空的MPC程序文件");
        }

        // 在适配器内读取 Spring Resource，业务层只接收普通字节数据。
        try (InputStream inputStream = resource.getInputStream()) {
            return new Artifact(
                    response.getBody().getData(),
                    resource.getFilename(),
                    inputStream.readAllBytes());
        }
    }
}
