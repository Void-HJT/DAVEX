package DveCenter.module.comparison;

import DveBase.common.R;
import DveBase.info.DirectoryInfo;
import DveBase.info.TableHeader;
import DveCenter.module.auth.service.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ComparisonService {

    @Autowired
    private CenterWebClientService centerWebClientService;

    public R<DirectoryInfo> getDirectory(Integer applicationId, Integer agentId) throws Exception {

        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        DirectoryInfo directoryInfo = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getDirectoryByApplication")
                        .queryParam("rootId", 0)
                        .queryParam("agentId", agentId)
                        .queryParam("applicationId", applicationId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<DirectoryInfo>>() {
                }).block().getBody().getData();

        return R.success(directoryInfo, "获取成功");
    }

    public R<TableHeader> getTableHeader(Integer applicationId, Integer agentId, Integer fileId, Integer folderId) throws Exception {

        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        TableHeader tableHeader = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/comparison/getcsvheader")
                        .queryParam("fileId", fileId)
                        .queryParam("folderId", folderId)
                        .queryParam("agentId", agentId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<TableHeader>>() {
                }).block().getBody().getData();


        return R.success(tableHeader, "获取成功");
    }
}
