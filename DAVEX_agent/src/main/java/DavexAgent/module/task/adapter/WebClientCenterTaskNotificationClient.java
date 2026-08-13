package DavexAgent.module.task.adapter;

import DavexAgent.module.task.port.CenterTaskNotificationClient;
import DavexBase.service.auth.AgentWebClientService;
import org.springframework.stereotype.Component;

/**
 * 使用现有 WebClient 工厂发送 Agent 到 Center 的任务通知。
 *
 * 通知接口地址及查询参数集中在此处，MPC Service 不再处理 HTTP 细节。
 */
@Component
public class WebClientCenterTaskNotificationClient
        implements CenterTaskNotificationClient {

    private final AgentWebClientService webClientService;

    public WebClientCenterTaskNotificationClient(
            AgentWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public void sendNotification(
            String centerId,
            String applicationId,
            String title,
            String content,
            String taskId,
            Integer code,
            String type) throws Exception {

        String response = webClientService
                .agent2CenterWebClient(centerId)
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/notification/set")
                        .queryParam("appID", applicationId)
                        .queryParam("title", title)
                        .queryParam("content", content)
                        .queryParam("taskID", taskId)
                        .queryParam("code", code)
                        .queryParam("type", type)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null) {
            throw new Exception("Center返回空通知响应");
        }
    }
}
