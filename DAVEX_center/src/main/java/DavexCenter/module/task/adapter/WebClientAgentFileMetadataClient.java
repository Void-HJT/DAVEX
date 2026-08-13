package DavexCenter.module.task.adapter;

import DavexBase.common.Body;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.task.port.AgentFileMetadataClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * 通过 HTTP 查询 Agent 输入文件的元数据。
 */
@Component
public class WebClientAgentFileMetadataClient
        implements AgentFileMetadataClient {

    private final CenterWebClientService webClientService;

    public WebClientAgentFileMetadataClient(CenterWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public long getRowCount(String agentId, String fileId) throws Exception {

        Body<Long> response = webClientService
                .center2AgentWebClient(agentId)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/directory/fileFolder/getRowCount")
                        .queryParam("agentId", agentId)
                        .queryParam("fileId", fileId)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Body<Long>>() {})
                .block();

        if (response == null || response.getCode() == 0 || response.getData() == null) {
            String message = response == null
                    ? "Agent返回空响应"
                    : response.getMessage();
            throw new Exception(message);
        }

        return response.getData();
    }
}
