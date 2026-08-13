package DavexAgent.module.task.adapter;

import DavexBase.common.R;
import DavexBase.entity.MpcTaskOutput;
import DavexBase.service.auth.AgentWebClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 验证 PSI 结果适配器保持 Multipart 上传端点和错误语义。 */
@ExtendWith(MockitoExtension.class)
class WebClientCenterMpcResultClientTest {

    @Mock
    private AgentWebClientService webClientService;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec postSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private WebClientCenterMpcResultClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new WebClientCenterMpcResultClient(webClientService);
        when(webClientService.agent2CenterWebClient("CENTER-1"))
                .thenReturn(webClient);
        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri("/MpcTasksOutput/save")).thenReturn(postSpec);
        when(postSpec.contentType(MediaType.MULTIPART_FORM_DATA))
                .thenReturn(postSpec);
        doReturn(headersSpec).when(postSpec).body(any(BodyInserter.class));
        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void uploadsPsiResultToStableEndpoint(@TempDir Path tempDir)
            throws Exception {
        Path result = tempDir.resolve("TASK-1.csv");
        Files.writeString(result, "id\n1001\n");
        MpcTaskOutput metadata = new MpcTaskOutput();
        metadata.setTaskId("TASK-1");
        doReturn(Mono.just(R.success("saved")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        client.uploadPsiResult("CENTER-1", result, metadata);

        verify(postSpec).uri("/MpcTasksOutput/save");
        verify(postSpec).contentType(MediaType.MULTIPART_FORM_DATA);
        verify(postSpec).body(any(BodyInserter.class));
    }

    @Test
    void propagatesCenterBusinessRejection(@TempDir Path tempDir)
            throws Exception {
        Path result = tempDir.resolve("TASK-1.csv");
        Files.writeString(result, "id\n1001\n");
        doReturn(Mono.just(R.error("结果保存失败")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        Exception error = assertThrows(
                Exception.class,
                () -> client.uploadPsiResult(
                        "CENTER-1", result, new MpcTaskOutput()));

        assertEquals("结果保存失败", error.getMessage());
    }
}
