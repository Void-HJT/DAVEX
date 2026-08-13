package DavexAgent.module.task.adapter;

import DavexBase.service.auth.AgentWebClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 验证通知适配器保持 Center 通知端点及查询参数协议。 */
@ExtendWith(MockitoExtension.class)
class WebClientCenterTaskNotificationClientTest {

    @Mock
    private AgentWebClientService webClientService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec postSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private WebClientCenterTaskNotificationClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new WebClientCenterTaskNotificationClient(webClientService);
        when(webClientService.agent2CenterWebClient("CENTER-1"))
                .thenReturn(webClient);
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(any(Function.class))).thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void sendsNotificationUsingStableQueryProtocol() throws Exception {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("success"));

        client.sendNotification(
                "CENTER-1", "APP-1", "PSI任务运行结束",
                "保存成功", "TASK-1", 1, "psi");

        ArgumentCaptor<Function> uriCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(postSpec).uri(uriCaptor.capture());
        UriBuilder builder = new DefaultUriBuilderFactory().builder();
        URI uri = (URI) uriCaptor.getValue().apply(builder);
        assertEquals(
                "/notification/set?appID=APP-1&title=PSI%E4%BB%BB%E5%8A%A1%E8%BF%90%E8%A1%8C%E7%BB%93%E6%9D%9F&content=%E4%BF%9D%E5%AD%98%E6%88%90%E5%8A%9F&taskID=TASK-1&code=1&type=psi",
                uri.toASCIIString());
    }

    @Test
    void rejectsEmptyCenterResponse() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.empty());

        Exception error = assertThrows(
                Exception.class,
                () -> client.sendNotification(
                        "CENTER-1", "APP-1", "title", "content",
                        "TASK-1", 0, "mpc"));

        assertEquals("Center返回空通知响应", error.getMessage());
    }
}
