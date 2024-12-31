package DavexCenter.module.task.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import DavexBase.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.info.UploadAgentTaskInfo;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.auth.CenterWebClientService;
import DavexBase.service.programs.GarnetService;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@ConditionalOnProperty(name = "garnet.enabled", havingValue = "true")
public class MpcTaskService {

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Autowired
    private InputMapper inputMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private My my;

    @Autowired
    private GarnetService garnetService;

    @Autowired
    private MpcTaskOutputService mpcTaskOutputService;

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private NotificationService notificationService;

    public Input createInput(Input input) {
        inputMapper.insert(input);
        return input;
    }

    public MpcTask getMpcTaskById(Long mpcTaskId) {
        return mpcTaskMapper.selectById(mpcTaskId);
    }

    public List<MpcTaskAgent> getAgentsByMpcTaskIdAndCenterId(Long mpcTaskId, Long centerId) {
        LambdaQueryWrapper<MpcTaskAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MpcTaskAgent::getMpcTaskId, mpcTaskId)
                .eq(MpcTaskAgent::getCenterId, centerId);
        return mpcTaskAgentMapper.selectList(queryWrapper);
    }

    public MpcTask getTaskByMpcTaskIdAndCenterId(Long mpcTaskId, Long centerId) {
        // 创建LambdaQueryWrapper实例
        LambdaQueryWrapper<MpcTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MpcTask::getUid, mpcTaskId)
                .eq(MpcTask::getCenterId, centerId);
        return mpcTaskMapper.selectOne(queryWrapper);
    }

    public UploadAgentTaskInfo create(UploadAgentTaskInfo mpcTaskInfo) throws Exception {
        if (!my.getId().equals(mpcTaskInfo.getCenterId())) {
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
        mpcTaskInfo = parameterUpdate(mpcTaskInfo);
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
            monos.add(centerWebClientService.center2AgentWebClient(partInfo.getAgentID()).post().uri("/MpcTasks/create")
                    .bodyValue((UploadAgentTaskInfo) mpcTaskInfoCopy).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<R<?>>() {
                    }));
        }
        Mono.when(monos).block();
        return mpcTaskInfo;
    }

    @Async("customExecutor")
    public void mpcRun(UploadAgentTaskInfo mpcTask) throws Exception {
        try {
            garnetService.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("MPC任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "MPC任务编译失败", errorContent, mpcTask.getUid(), 0, "mpc");
            throw e;
        }
        garnetService.link(Paths.get(my.getBase_path())
                .resolve(
                        inputMapper.selectById(mpcTask.getDataId()).getPath())
                .toString(),
                mpcTask.getUid(), mpcTask.getPart());
        while (ready(mpcTask.getUid()) == false) {
            Thread.sleep(1000);
        }
        if (run(mpcTask.getUid()) == false) {
            throw new Exception("任务未就绪");
        }
        try {
            garnetService.run(mpcTask);
        } catch (Exception e) {
            // 运行失败的通知
            String errorContent = String.format("MPC任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "MPC任务运行失败", errorContent, mpcTask.getUid(), 0, "mpc");
            throw e;
        }
        mpcTaskOutputService.saveOutputFromInner(mpcTaskMapper.selectById(mpcTask.getUid()));
    }

    @Async("customExecutor")
    public void psiRun(UploadAgentTaskInfo mpcTask) throws Exception {
        try {
            garnetService.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("PSI任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "PSI任务编译失败", errorContent, mpcTask.getUid(), 0, "psi");
            throw e;
        }
        garnetService.idExtract(inputMapper.selectById(mpcTask.getDataId()).getPath(), mpcTask.getUid(),
                mpcTask.getPart());
        while (ready(mpcTask.getUid()) == false) {
            Thread.sleep(1000);
        }
        if (run(mpcTask.getUid()) == false) {
            throw new Exception("任务未就绪");
        }
        try {
            garnetService.run(mpcTask);
        } catch (Exception e) {
            // 运行失败的通知
            String errorContent = String.format("PSI任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "PSI任务运行失败", errorContent, mpcTask.getUid(), 0, "psi");
            throw e;
        }
        // * 什么也不做，结果由Agent返回
    }

    public Boolean ready(String mpcTaskId) throws Exception {
        MpcTask mpcTask = mpcTaskMapper.selectById(mpcTaskId);
        if (mpcTask == null) {
            throw new Exception("任务不存在");
        }
        switch (mpcTask.getStatus()) {
            case READY:
                break;
            case COMPILING:
                return false;
            case RUNNING:
                throw new Exception("任务已在运行中");
            case FAILED:
                throw new Exception("任务失败");
            case FINISHED:
                throw new Exception("任务已完成");
            default:
                throw new Exception("错误");
        }
        LambdaQueryWrapper<MpcTaskAgent> queryWrapper = Wrappers.<MpcTaskAgent>lambdaQuery()
                .eq(MpcTaskAgent::getMpcTaskId, mpcTaskId);
        List<MpcTaskAgent> agents = mpcTaskAgentMapper.selectList(queryWrapper);
        List<R<Boolean>> responses = Flux.fromIterable(agents).flatMap((MpcTaskAgent a) -> {
            try {
                return centerWebClientService.center2AgentWebClient(a.getAgentId()).get()
                        .uri(uriBuilder -> uriBuilder.path("/MpcTasks/ready").queryParam("mpcTaskId", mpcTaskId)
                                .build())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<R<Boolean>>() {
                        });
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collectList().block();
        for (R<Boolean> response : responses) {
            if (response.getBody().getCode() == 0) {
                throw new Exception(response.getBody().getMessage());
            }
            if (!response.getBody().getData()) {
                return false;
            }
        }
        return true;
    }

    public Boolean run(String mpcTaskId) throws Exception {
        MpcTask mpcTask = mpcTaskMapper.selectById(mpcTaskId);
        if (mpcTask == null) {
            throw new Exception("任务不存在");
        }
        LambdaQueryWrapper<MpcTaskAgent> queryWrapper = Wrappers.<MpcTaskAgent>lambdaQuery()
                .eq(MpcTaskAgent::getMpcTaskId, mpcTaskId);
        List<MpcTaskAgent> agents = mpcTaskAgentMapper.selectList(queryWrapper);
        List<R<Boolean>> responses = Flux.fromIterable(agents).flatMap((MpcTaskAgent a) -> {
            try {
                return centerWebClientService.center2AgentWebClient(a.getAgentId()).get()
                        .uri(uriBuilder -> uriBuilder.path("/MpcTasks/run").queryParam("mpcTaskId", mpcTaskId)
                                .build())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<R<Boolean>>() {
                        });
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collectList().block();
        for (R<Boolean> response : responses) {
            if (response.getBody().getCode() == 0) {
                throw new Exception(response.getBody().getMessage());
            }
            if (!response.getBody().getData()) {
                return false;
            }
        }
        return true;
    }

    public List<MpcTask> list() {
        return mpcTaskMapper.selectList(null);
    }

    public UploadAgentTaskInfo parameterUpdate(UploadAgentTaskInfo mpcTask) {
        MpcTask.TaskType type = mpcTask.getTaskType();
        if (type == MpcTask.TaskType.GARNET_PSI) {
            JSONObject compileParameters = new JSONObject();
            Long P0_data = garnetService.csvCount(inputMapper.selectById(mpcTask.getDataId()).getPath());
            Long P1_data = null;
            UploadAgentTaskInfo.PartInfo p1 = mpcTask.getPartInfo().get(0);
            try {
                P1_data = centerWebClientService.center2AgentWebClient(p1.getAgentID()).post()
                        .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getRowCount")
                                .queryParam("agentId", p1.getAgentID())
                                .queryParam("fileId", p1.getFileID())
                                .build())
                        .retrieve().bodyToMono(new ParameterizedTypeReference<Body<Long>>() {
                        }).block().getData();
            } catch (Exception e) {
                e.printStackTrace();
                return mpcTask;
            }
            compileParameters.put("P0_Data", P0_data);
            // directory/fileFolder/getRowCoun得到的csv行数包含表头
            compileParameters.put("P1_Data", P1_data - 1);
            mpcTask.setCompileParameters(compileParameters);
        }
        return mpcTask;
    }
}
