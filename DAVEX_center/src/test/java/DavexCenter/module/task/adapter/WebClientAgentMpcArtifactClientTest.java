package DavexCenter.module.task.adapter;

import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.task.port.AgentMpcArtifactClient;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 验证 Agent MPC 程序包适配器保持查询、下载端点及返回数据。 */
@ExtendWith(MockitoExtension.class)
class WebClientAgentMpcArtifactClientTest {

    @Mock
    private CenterWebClientService webClientService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec<?> getSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private Resource resource;

    private WebClientAgentMpcArtifactClient client;

    @BeforeEach
    @SuppressWarnings({"rawtypes", "unchecked"})
    void setUp() throws Exception {
        client = new WebClientAgentMpcArtifactClient(webClientService);
        when(webClientService.center2AgentWebClient("AGENT-1"))
                .thenReturn(webClient);
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) getSpec);
        doReturn(headersSpec).when(getSpec).uri(any(Function.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void fetchesMetadataAndProgramUsingStableEndpoints() throws Exception {
        Mpc mpc = new Mpc();
        mpc.setUid("MPC-1");
        byte[] content = new byte[] {1, 2, 3};
        doReturn(Mono.just(R.success(mpc, "ok")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));
        when(responseSpec.bodyToMono(Resource.class))
                .thenReturn(Mono.just(resource));
        when(resource.getFilename()).thenReturn("demo.mpc");
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream(content));

        AgentMpcArtifactClient.Artifact artifact =
                client.fetchArtifact("AGENT-1", "MPC-1");

        assertSame(mpc, artifact.metadata());
        assertEquals("demo.mpc", artifact.fileName());
        assertArrayEquals(content, artifact.content());

        ArgumentCaptor<Function> uriCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(getSpec, times(2)).uri(uriCaptor.capture());
        List<Function> builders = uriCaptor.getAllValues();
        UriBuilder builder = new DefaultUriBuilderFactory().builder();
        URI selectUri = (URI) builders.get(0).apply(builder);
        builder = new DefaultUriBuilderFactory().builder();
        URI downloadUri = (URI) builders.get(1).apply(builder);
        assertEquals("/Mpc/select?MpcID=MPC-1", selectUri.toASCIIString());
        assertEquals("/Mpc/download?MpcID=MPC-1", downloadUri.toASCIIString());
    }
}
