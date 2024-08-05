package DveAgent.module.task.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import DveAgent.common.MyAgent;
import DveAgent.module.auth.service.AgentWebClientService;
import DveBase.common.R;
import DveBase.common.Utils;
import DveBase.entity.Mpc;
import DveBase.mapper.MpcMapper;

@Service
public class MpcService {
    @Autowired
    private AgentWebClientService agentWebClientService;

    @Autowired
    private MyAgent my;

    @Autowired
    private MpcMapper mpcMapper;

    public void downloadFile(Long centerId, String MpcID) throws Exception {
        WebClient webClient = agentWebClientService.agent2CenterWebClient(centerId);
        Mpc mpc = webClient.get()
                .uri(UriBuilder -> UriBuilder.path("/Mpc/select").queryParam("MpcID", MpcID).build()).retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<Mpc>>() {
                }).block().getBody().getData();
        webClient.get()
                .uri(UriBuilder -> UriBuilder.path("/Mpc/download").queryParam("MpcID", MpcID).build()).retrieve()
                .bodyToMono(Resource.class).subscribe(resource -> {
                    Path filePath = Paths.get(my.getBase_path()).resolve("programs").resolve(resource.getFilename());
                    filePath = Utils.resolveFileNameConflict(filePath);
                    try {
                        Files.copy(resource.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        mpc.setPath(Paths.get("programs").resolve(filePath.getFileName()).toString());
                        mpcMapper.insert(mpc);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

}
