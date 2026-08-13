package DavexCenter.module.task.adapter;

import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.task.port.AgentSecureInferenceClient;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 验证安全推理适配器保持创建任务、查询模型和下载程序的协议。 */
@ExtendWith(MockitoExtension.class)
class WebClientAgentSecureInferenceClientTest {

    @Mock
    private CenterWebClientService webClientService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec postSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec<?> getSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private Resource resource;

    private WebClientAgentSecureInferenceClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new WebClientAgentSecureInferenceClient(webClientService);
        when(webClientService.center2AgentWebClient("AGENT-1"))
                .thenReturn(webClient);
    }

    @Test
    void createsTaskUsingStableEndpointAndContract() throws Exception {
        MpcTaskCreateRequest request = request();
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri("/SecureInference/create")).thenReturn(postSpec);
        doReturn(headersSpec).when(postSpec).bodyValue(request);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.success("created")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        client.createTasks(List.of(
                new AgentSecureInferenceClient.TaskDispatch(
                        "AGENT-1", request)));

        verify(postSpec).uri("/SecureInference/create");
        verify(postSpec).bodyValue(request);
    }

    @Test
    void propagatesAgentBusinessRejection() throws Exception {
        MpcTaskCreateRequest request = request();
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri("/SecureInference/create")).thenReturn(postSpec);
        doReturn(headersSpec).when(postSpec).bodyValue(request);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.error("Agent拒绝推理任务")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> client.createTasks(List.of(
                        new AgentSecureInferenceClient.TaskDispatch(
                                "AGENT-1", request))));

        assertEquals("Agent拒绝推理任务", error.getMessage());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void fetchesModelMetadataAndProgramUsingStableEndpoints()
            throws Exception {
        Mpc mpc = new Mpc();
        mpc.setUid("MPC-1");
        byte[] content = new byte[] {1, 2, 3};
        when(webClient.get()).thenReturn(
                (WebClient.RequestHeadersUriSpec) getSpec);
        doReturn(headersSpec).when(getSpec).uri(any(Function.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.success(mpc, "ok")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));
        when(responseSpec.bodyToMono(Resource.class))
                .thenReturn(Mono.just(resource));
        when(resource.getFilename()).thenReturn("inference.mpc");
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream(content));

        AgentSecureInferenceClient.Artifact artifact =
                client.fetchArtifact("AGENT-1", "MODEL-1");

        assertSame(mpc, artifact.metadata());
        assertEquals("inference.mpc", artifact.fileName());
        assertArrayEquals(content, artifact.content());

        ArgumentCaptor<Function> uriCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(getSpec, times(2)).uri(uriCaptor.capture());
        List<Function> builders = uriCaptor.getAllValues();
        UriBuilder builder = new DefaultUriBuilderFactory().builder();
        URI metadataUri = (URI) builders.get(0).apply(builder);
        builder = new DefaultUriBuilderFactory().builder();
        URI downloadUri = (URI) builders.get(1).apply(builder);
        assertEquals("/SecureInference/getMpc?FileID=MODEL-1",
                metadataUri.toASCIIString());
        assertEquals("/Mpc/download?MpcID=MPC-1",
                downloadUri.toASCIIString());
    }

    private MpcTaskCreateRequest request() {
        return new MpcTaskCreateRequest(
                "TASK-1", "APP-1", "CENTER-1", "MPC-1",
                null, null, 2, 1L, "127.0.0.1", 6099,
                "MODEL-1", MpcTaskCreateRequest.TaskType.GARNET_INFERENCE,
                MpcTaskCreateRequest.Status.INIT, List.of());
    }
}
