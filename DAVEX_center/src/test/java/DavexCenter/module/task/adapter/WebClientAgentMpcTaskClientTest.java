package DavexCenter.module.task.adapter;

import DavexBase.common.R;
import DavexBase.service.auth.CenterWebClientService;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 验证 MPC 通信适配器保持既有 URI、请求体和响应错误语义。 */
@ExtendWith(MockitoExtension.class)
class WebClientAgentMpcTaskClientTest {

    @Mock
    private CenterWebClientService webClientService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec postSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec<?> getSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private WebClientAgentMpcTaskClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new WebClientAgentMpcTaskClient(webClientService);
        when(webClientService.center2AgentWebClient("AGENT-1"))
                .thenReturn(webClient);
    }

    @Test
    void createsTaskUsingStableContractAndEndpoint() throws Exception {
        MpcTaskCreateRequest request = request();
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri("/MpcTasks/create")).thenReturn(postSpec);
        doReturn(headersSpec).when(postSpec).bodyValue(request);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.success("created")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        client.createTask("AGENT-1", request);

        verify(postSpec).uri("/MpcTasks/create");
        verify(postSpec).bodyValue(request);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void mapsReadyAndRunResponseData() throws Exception {
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) getSpec);
        doReturn(headersSpec).when(getSpec).uri(any(java.util.function.Function.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.success(false, "not ready")),
                Mono.just(R.success(true, "running")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        assertFalse(client.isReady("AGENT-1", "TASK-1"));
        assertTrue(client.runTask("AGENT-1", "TASK-1"));
    }

    @Test
    void propagatesRemoteBusinessRejection() throws Exception {
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri("/MpcTasks/create")).thenReturn(postSpec);
        doReturn(headersSpec).when(postSpec).bodyValue(any());
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.error("Agent拒绝任务")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        Exception error = assertThrows(
                Exception.class,
                () -> client.createTask("AGENT-1", request()));

        org.junit.jupiter.api.Assertions.assertEquals(
                "Agent拒绝任务", error.getMessage());
    }

    private MpcTaskCreateRequest request() {
        return new MpcTaskCreateRequest(
                "TASK-1", "APP-1", "CENTER-1", "MPC-1",
                null, null, 2, 1L, "127.0.0.1", 8080,
                "FILE-1", MpcTaskCreateRequest.TaskType.GARNET_MPC,
                MpcTaskCreateRequest.Status.INIT, List.of());
    }
}
