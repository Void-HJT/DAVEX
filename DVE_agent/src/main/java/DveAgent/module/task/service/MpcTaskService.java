
package DveAgent.module.task.service;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import DveAgent.common.My;
import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveAgent.info.UploadAgentTaskInfo;
import DveAgent.mapper.FileMapper;
import DveAgent.mapper.MpcTaskAgentMapper;
import DveAgent.mapper.MpcTaskMapper;
import DveAgent.module.directory.FileFolderService;

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
        garnetService.link(fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), "/home/nhy"),
                mpcTask.getUid(),
                Long.valueOf(mpcTask.getPart().toString()));
    }

    @Async("customExecutor")
    private void psiPreprocess(UploadAgentTaskInfo mpcTask) throws Exception {
        garnetService.compile(mpcTask);
        garnetService.csvExtract(fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), "/home/nhy"),
                mpcTask.getRuntimeParameters().getString("PK"), mpcTask.getUid());
    }

    @Async("customExecutor")
    public void run(MpcTask mpcTask) throws Exception {
        garnetService.run(mpcTask);
    }

    @Async("customExecutor")
    public void psiRun(MpcTask mpcTask) throws Exception {
        garnetService.run(mpcTask);
        garnetService.csvQuery(fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), "/home/nhy"),
                mpcTask.getUid(), mpcTask.getRuntimeParameters().getString("PK"), "/home/nhy/DVE/output.csv");
    }

    public Boolean ready(String mpcTaskId) throws Exception {
        return mpcTaskMapper.selectById(mpcTaskId).getReady();
    }
}