
package DveAgent.module.task.service;

import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import DveAgent.common.MyAgent;
import DveAgent.module.auth.service.AgentWebClientService;
import DveAgent.module.directory.service.FileFolderService;
import DveBase.common.Utils;
import DveBase.entity.MpcTask;
import DveBase.entity.MpcTaskAgent;
import DveBase.entity.MpcTaskOutput;
import DveBase.info.UploadAgentTaskInfo;
import DveBase.mapper.FileMapper;
import DveBase.mapper.MpcMapper;
import DveBase.mapper.MpcTaskAgentMapper;
import DveBase.mapper.MpcTaskMapper;

@Service
public class MpcTaskService {

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Autowired
    private MyAgent my;

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

    // TODO 检查File权限
    public void createMpcTask(UploadAgentTaskInfo mpctTaskInfo) throws Exception {
        if (mpctTaskInfo.getUid() != null && mpcTaskMapper.selectById(mpctTaskInfo.getUid()) != null) {
            throw new Exception("任务已存在");
        }
        Map<Long, Pair<Long, Long>> map = mpctTaskInfo.getAgentID2fileID();
        if (!map.containsKey(my.getId())) {
            throw new Exception("发送错误");
        }
        mpctTaskInfo.setDataId(map.get(my.getId()).getRight());
        // TODO 取消强制类型转换
        mpctTaskInfo.setPart(Math.toIntExact(map.get(my.getId()).getLeft()));
        for (Map.Entry<Long, Pair<Long, Long>> entry : map.entrySet()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(entry.getKey());
            mpcTaskAgent.setPart(entry.getValue().getLeft());
            mpcTaskAgent.setMpcTaskId(mpctTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpctTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
        }
        if (mpcMapper.selectById(mpctTaskInfo.getMpcId()) == null) {
            mpcService.downloadFile(mpctTaskInfo.getCenterId(), mpctTaskInfo.getMpcId());
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
    private void preprocess(UploadAgentTaskInfo mpcTask) throws Exception {
        garnetService.compile(mpcTask);
        garnetService.link(fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getUid(),
                Long.valueOf(mpcTask.getPart().toString()));
    }

    @Async("customExecutor")
    private void psiPreprocess(UploadAgentTaskInfo mpcTask) throws Exception {
        garnetService.compile(mpcTask);
        garnetService.csvExtract(
                fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getRuntimeParameters().getString("PK"), mpcTask.getUid(), mpcTask.getPart());
    }

    @Async("customExecutor")
    public void run(MpcTask mpcTask) throws Exception {
        garnetService.run(mpcTask);
    }

    @Async("customExecutor")
    public void psiRun(MpcTask mpcTask) throws Exception {
        garnetService.run(mpcTask);
        String filePath = Paths.get(my.getBase_path()).resolve("mpctask").resolve(mpcTask.getUid() + ".csv").toString();
        garnetService.csvQuery(
                fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getUid(), mpcTask.getRuntimeParameters().getString("PK"), mpcTask.getPart(),
                filePath);
        FileSystemResource fileResource = new FileSystemResource(filePath);
        MpcTaskOutput mpcTaskOutput = new MpcTaskOutput();
        mpcTaskOutput.setTaskId(mpcTask.getUid());
        mpcTaskOutput.setHash(Utils.getFileHash(fileResource, "SHA-256"));
        agentWebClientService.agent2CenterWebClient(mpcTask.getCenterId()).post().uri("/MpcTasks/save")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(BodyInserters.fromMultipartData("file", fileResource)
                        .with("metadata", mpcTaskOutput))
                .retrieve().bodyToMono(String.class).block();
    }

    public Boolean ready(String mpcTaskId) throws Exception {
        return mpcTaskMapper.selectById(mpcTaskId).getStatus() == MpcTask.Status.READY;
    }
}