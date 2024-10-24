package DavexAgent.module.task.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.common.Utils;
import DavexBase.entity.Mpc;
import DavexBase.mapper.MpcMapper;
import DavexBase.service.auth.AgentWebClientService;
import DavexBase.service.programs.GarnetService;

@Service
public class MpcService {
    @Autowired
    private AgentWebClientService agentWebClientService;

    @Autowired
    private My my;

    @Autowired
    private MpcMapper mpcMapper;

    private static final Logger logger = LoggerFactory.getLogger(GarnetService.class);

    public void downloadMPC(String centerId, String MpcID) throws Exception {
        WebClient webClient = agentWebClientService.agent2CenterWebClient(centerId);
        Mpc mpc = webClient.get()
                .uri(UriBuilder -> UriBuilder.path("/Mpc/select").queryParam("MpcID", MpcID).build()).retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<Mpc>>() {
                }).block().getBody().getData();
        Resource resource = webClient.get()
                .uri(UriBuilder -> UriBuilder.path("/Mpc/download").queryParam("MpcID", MpcID).build())
                .retrieve()
                .bodyToMono(Resource.class)
                .block();

        if (resource != null) {
            Path filePath = Paths.get(my.getBase_path()).resolve("programs").resolve(resource.getFilename());
            filePath = Utils.resolveFileNameConflict(filePath);
            try {
                Files.createDirectories(filePath.getParent());
                Files.copy(resource.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                mpc.setPath(Paths.get("programs").resolve(filePath.getFileName()).toString());
                mpcMapper.insert(mpc);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
        logger.info("成功下载" + mpc.getUid() + " : " + mpc.getName());
        return;
    }

}
