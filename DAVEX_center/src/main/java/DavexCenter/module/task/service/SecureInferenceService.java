package DavexCenter.module.task.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.common.Utils;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask.TaskType;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.info.InferenceInfo;
import DavexBase.info.UploadAgentTaskInfo;
import DavexBase.info.UploadAgentTaskInfo.PartInfo;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.auth.CenterWebClientService;
import reactor.core.publisher.Mono;

@Service
public class SecureInferenceService {
    @Autowired
    private MpcTaskService mpcTaskService;
    @Autowired
    private MpcMapper mpcMapper;
    @Autowired
    private MpcTaskMapper mpcTaskMapper;
    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private My my;
    @Autowired
    private CenterWebClientService centerWebClientService;
    private static final Logger logger = LoggerFactory.getLogger(SecureInferenceService.class);

    public UploadAgentTaskInfo create(UploadAgentTaskInfo mpcTaskInfo) throws Exception {
        if (mpcTaskInfo.getTaskType() != TaskType.GARNET_INFERENCE) {
            throw new Exception("任务类型不匹配");
        }
        if (mpcTaskInfo.getCenterId() != my.getId()) {
            throw new Exception("发送错误");
        }
        for (UploadAgentTaskInfo.PartInfo partInfo : mpcTaskInfo.getPartInfo()) {
            if (agentMapper.selectById(partInfo.getAgentID()) == null) {
                throw new Exception("Agent不存在");
            }
            // TODO 检查app有权访问File
            // if (fileMapper.selectById(entry.getValue().getRight()) == null) {
            // throw new Exception("文件不存在");
            // }
        }
        mpcTaskMapper.insert(mpcTaskInfo);
        List<Mono<R<?>>> monos = new ArrayList<Mono<R<?>>>();
        for (UploadAgentTaskInfo.PartInfo partInfo : mpcTaskInfo.getPartInfo()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(partInfo.getAgentID());
            mpcTaskAgent.setPart(partInfo.getPart());
            mpcTaskAgent.setMpcTaskId(mpcTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpcTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
            UploadAgentTaskInfo mpcTaskInfoCopy = new UploadAgentTaskInfo(mpcTaskInfo);
            mpcTaskInfoCopy.maskFileID();
            mpcTaskInfoCopy.setPart(partInfo.getPart());
            mpcTaskInfoCopy.setDataId(partInfo.getFileID());
            monos.add(centerWebClientService.center2AgentWebClient(partInfo.getAgentID()).post()
                    .uri("/SecureInference/create")
                    .bodyValue((UploadAgentTaskInfo) mpcTaskInfoCopy).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<R<?>>() {
                    }));
        }
        Mono.when(monos).block();
        return mpcTaskInfo;
    }

    @Async("customExecutor")
    public Mpc downloadMPC(String agentId, String fileID) throws Exception {
        WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
        Mpc mpc = webClient.get()
                .uri(UriBuilder -> UriBuilder.path("/SecureInference/getMpc").queryParam("FileID", fileID)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<Mpc>>() {
                }).block().getBody().getData();
        if (mpcMapper.selectById(mpc.getUid()) != null) {
            logger.info("MPC文件：{} 已存在", mpc.getUid());
            return mpc;
        }
        logger.info("开始下载mpc文件：{}", mpc.getUid());
        Resource resource = webClient.get()
                .uri(UriBuilder -> UriBuilder.path("/Mpc/download").queryParam("MpcID", mpc.getUid()).build())
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
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        }
        return mpc;
    }

    @Async("customExecutor")
    public void run(UploadAgentTaskInfo mpcTask) throws Exception {
        mpcTaskService.mpcRun(mpcTask);
    }

    public UploadAgentTaskInfo wrapMpcTaskInfo(InferenceInfo info) throws Exception {
        logger.info("接收安全推理任务：{}", info);
        UploadAgentTaskInfo mpcTaskInfo = new UploadAgentTaskInfo();
        mpcTaskInfo.setHost(my.getIp());
        mpcTaskInfo.setPort(10099);
        mpcTaskInfo.setN(2);
        mpcTaskInfo.setApplicationId(info.getApplicationId());
        mpcTaskInfo.setTaskType(TaskType.GARNET_INFERENCE);
        mpcTaskInfo.setCenterId(my.getId());
        mpcTaskInfo.setPart(0L);
        List<PartInfo> partInfo = new ArrayList<>();
        partInfo.add(new PartInfo() {
            {
                setAgentID(info.getAgentId());
                setPart(1L);
                setFileID(info.getFileId());
            }
        });
        mpcTaskInfo.setPartInfo(partInfo);
        mpcTaskInfo.setMpcId(downloadMPC(info.getAgentId(), info.getFileId()).getUid());
        // * 使用默认参数
        mpcTaskInfo.useDefault(mpcMapper.selectById(mpcTaskInfo.getMpcId()));
        return mpcTaskInfo;
    }
}
