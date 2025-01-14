
package DavexAgent.module.task.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import DavexBase.common.Body;
import DavexBase.entity.File;
import DavexBase.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.common.Utils;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.entity.MpcTaskOutput;
import DavexBase.info.UploadAgentTaskInfo;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.auth.AgentWebClientService;
import DavexBase.service.directory.FileFolderService;
import DavexBase.service.programs.GarnetService;

@Service
public class MpcTaskService {

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Autowired
    private My my;

    @Autowired
    GarnetService garnetService;

    @Autowired
    AgentWebClientService agentWebClientService;

    @Autowired
    MpcMapper mpcMapper;

    @Autowired
    MpcService mpcService;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    FileFolderService fileFolderService;

    @Autowired
    NotificationService notificationService;

    // TODO 检查File权限
    public void createMpcTask(UploadAgentTaskInfo mpctTaskInfo) throws Exception {

        if (mpctTaskInfo.getUid() != null && mpcTaskMapper.selectById(mpctTaskInfo.getUid()) != null) {
            throw new Exception("任务已存在");
        }
        for (UploadAgentTaskInfo.PartInfo partInfo : mpctTaskInfo.getPartInfo()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(partInfo.getAgentID());
            mpcTaskAgent.setPart(partInfo.getPart());
            mpcTaskAgent.setMpcTaskId(mpctTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpctTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
        }
        if (mpcMapper.selectById(mpctTaskInfo.getMpcId()) == null) {
            mpcService.downloadMPC(mpctTaskInfo.getCenterId(), mpctTaskInfo.getMpcId());
        }
        mpcTaskMapper.insert(mpctTaskInfo);

        switch (mpctTaskInfo.getTaskType()) {
            case GARNET_MPC:
            default:
                preprocess(mpctTaskInfo);
                break;
            case GARNET_PSI:
                psiPreprocess(mpctTaskInfo);
                break;
        }
    }

    @Async("customExecutor")
    public void preprocess(UploadAgentTaskInfo mpcTask) throws Exception {
        try {
            garnetService.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("MPC任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri(uriBuilder -> uriBuilder.path("/notification/set")
                            .queryParam("appID", mpcTask.getApplicationId())
                            .queryParam("title", "MPC任务编译失败")
                            .queryParam("content", errorContent)
                            .queryParam("taskID", mpcTask.getUid())
                            .queryParam("code", 0)
                            .queryParam("type", "mpc").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    }).block();
            throw e;
        }
        garnetService.link(fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getUid(),
                mpcTask.getPart());
    }

    @Async("customExecutor")
    private void psiPreprocess(UploadAgentTaskInfo mpcTask) throws Exception {
        try {
            garnetService.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("PSI任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri(uriBuilder -> uriBuilder.path("/notification/set")
                            .queryParam("appID", mpcTask.getApplicationId())
                            .queryParam("title", "PSI任务编译失败")
                            .queryParam("content", errorContent)
                            .queryParam("taskID", mpcTask.getUid())
                            .queryParam("code", 0)
                            .queryParam("type", "psi").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    }).block();
            throw e;
        }
        garnetService.csvExtract(
                fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getRuntimeParameters().getString("PK"), mpcTask.getUid(), mpcTask.getPart());
    }

    @Async("customExecutor")
    public void run(MpcTask mpcTask) throws Exception {
        try {
            garnetService.run(mpcTask);
        } catch (Exception e) {
            // 运行失败的通知
            String errorContent = String.format("MPC任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri(uriBuilder -> uriBuilder.path("/notification/set")
                            .queryParam("appID", mpcTask.getApplicationId())
                            .queryParam("title", "MPC任务运行失败")
                            .queryParam("content", errorContent)
                            .queryParam("taskID", mpcTask.getUid())
                            .queryParam("code", 0)
                            .queryParam("type", "mpc").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    }).block();
            throw e;
        }
        // * 通常来说，MPC任务结果不需要返回。
        // String outputPath = my.getGarnet_path() + "/Output/" + mpcTask.getUid() +
        // "-P" + mpcTask.getPart() + "-0";
        // FileSystemResource fileResource = new FileSystemResource(outputPath);
        // MpcTaskOutput mpcTaskOutput = new MpcTaskOutput();
        // mpcTaskOutput.setTaskId(mpcTask.getUid());
        // mpcTaskOutput.setHash(Utils.getFileHash(fileResource, "SHA-256"));
        // agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post().uri("/MpcTasks/save")
        // .contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData("file",
        // fileResource)
        // .with("metadata", mpcTaskOutput))
        // .retrieve().bodyToMono(new ParameterizedTypeReference<R<String>>() {
        // }).block();
    }

    @Async("customExecutor")
    public void psiRun(MpcTask mpcTask) throws Exception {
        try {
            garnetService.run(mpcTask);
        } catch (Exception e) {
            // 任务执行失败的通知
            String errorContent = String.format("PSI任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri(uriBuilder -> uriBuilder.path("/notification/set")
                            .queryParam("appID", mpcTask.getApplicationId())
                            .queryParam("title", "PSI任务运行失败")
                            .queryParam("content", errorContent)
                            .queryParam("taskID", mpcTask.getUid())
                            .queryParam("code", 0)
                            .queryParam("type", "psi").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    }).block();
            throw e;
        }
        try {
            Path filePath = Paths.get(my.getBase_path()).resolve("mpctask").resolve(mpcTask.getUid() + ".csv");
            Files.createDirectories(filePath.getParent());
            garnetService.csvQuery(
                    fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                    mpcTask.getUid(), mpcTask.getRuntimeParameters().getString("PK"), mpcTask.getPart(),
                    filePath);
            FileSystemResource fileResource = new FileSystemResource(filePath);
            MpcTaskOutput mpcTaskOutput = new MpcTaskOutput();
            mpcTaskOutput.setTaskId(mpcTask.getUid());
            mpcTaskOutput.setHash(Utils.getFileHash(fileResource, "SHA-256"));
            mpcTaskOutput.setApplicationId(mpcTask.getApplicationId());
            mpcTaskOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
            mpcTaskOutput.setName(mpcTask.getUid() + ".csv");
            R<String> res = agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri("/MpcTasksOutput/save")
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData("file", fileResource)
                            .with("metadata", mpcTaskOutput))
                    .retrieve().bodyToMono(new ParameterizedTypeReference<R<String>>() {
                    }).block();
            System.out.println(res);

            // 成功保存结果的消息通知
            String content = String.format("PSI任务结果保存成功\n任务ID: %s\n任务类型: %s\n运行结果: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), mpcTask.getStatus());
            Integer code = 1;

            agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri(uriBuilder -> uriBuilder.path("/notification/set")
                            .queryParam("appID", mpcTask.getApplicationId())
                            .queryParam("title", "PSI任务运行结束")
                            .queryParam("content", content)
                            .queryParam("taskID", mpcTask.getUid())
                            .queryParam("code", code)
                            .queryParam("type", "psi").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    }).block();
        } catch (Exception e) {
            e.printStackTrace();

            // 保存失败消息通知
            String content = String.format("PSI任务结果保存失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            Integer code = 0;

            agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post()
                    .uri(uriBuilder -> uriBuilder.path("/notification/set")
                            .queryParam("appID", mpcTask.getApplicationId())
                            .queryParam("title", "PSI任务运行结束")
                            .queryParam("content", content)
                            .queryParam("taskID", mpcTask.getUid())
                            .queryParam("code", code)
                            .queryParam("type", "psi").build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<String>() {
                    }).block();
        }
    }

    public Boolean ready(String mpcTaskId) throws Exception {
        return mpcTaskMapper.selectById(mpcTaskId).getStatus() == MpcTask.Status.READY;
    }
}