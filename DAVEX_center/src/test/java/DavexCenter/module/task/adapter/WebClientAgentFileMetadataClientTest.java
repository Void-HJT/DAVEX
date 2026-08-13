package DavexCenter.module.task.adapter;

import DavexBase.common.Body;
import DavexBase.service.auth.CenterWebClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/** 验证 PSI 文件行数查询被限制在文件元数据通信适配器中。 */
@ExtendWith(MockitoExtension.class)
class WebClientAgentFileMetadataClientTest {

    @Mock
    private CenterWebClientService webClientService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec postSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private WebClientAgentFileMetadataClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new WebClientAgentFileMetadataClient(webClientService);
        when(webClientService.center2AgentWebClient("AGENT-1"))
                .thenReturn(webClient);
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(any(java.util.function.Function.class)))
                .thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void returnsAgentFileRowCount() throws Exception {
        doReturn(Mono.just(Body.success(11L, "ok")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        assertEquals(11L, client.getRowCount("AGENT-1", "FILE-1"));
    }

    @Test
    void rejectsEmptyRemoteResponse() {
        doReturn(Mono.empty())
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        Exception error = assertThrows(
                Exception.class,
                () -> client.getRowCount("AGENT-1", "FILE-1"));

        assertEquals("Agent返回空响应", error.getMessage());
    }
}
