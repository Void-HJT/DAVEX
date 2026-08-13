package DavexAgent.module.task.adapter;

import java.nio.file.Path;

import DavexAgent.module.task.port.CenterMpcResultClient;
import DavexBase.common.R;
import DavexBase.entity.MpcTaskOutput;
import DavexBase.service.auth.AgentWebClientService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * 使用 Multipart 请求向 Center 上传 PSI 结果文件和结果元数据。
 */
@Component
public class WebClientCenterMpcResultClient
        implements CenterMpcResultClient {

    private final AgentWebClientService webClientService;

    public WebClientCenterMpcResultClient(
            AgentWebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public void uploadPsiResult(
            String centerId,
            Path resultFile,
            MpcTaskOutput metadata) throws Exception {

        FileSystemResource fileResource =
                new FileSystemResource(resultFile);

        R<String> response = webClientService
                .agent2CenterWebClient(centerId)
                .post()
                .uri("/MpcTasksOutput/save")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters
                        .fromMultipartData("file", fileResource)
                        .with("metadata", metadata))
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<R<String>>() {
                        })
                .block();

        if (response == null || response.getBody() == null) {
            throw new Exception("Center返回空结果响应");
        }

        if (response.getBody().getCode() == 0) {
            throw new Exception(response.getBody().getMessage());
        }
    }
}
