package DavexCenter.module.task.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.info.UploadAgentTaskInfo;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.programs.GarnetService;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.auth.service.CenterWebClientService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
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

    public void create(UploadAgentTaskInfo mpctTaskInfo) throws Exception {
        if (mpctTaskInfo.getCenterId() != my.getId()) {
            throw new Exception("发送错误");
        }

        if (mpctTaskInfo.getUid() != null && mpcTaskMapper.selectById(mpctTaskInfo.getUid()) != null) {
            throw new Exception("任务已存在");
        }

        for (Map.Entry<Long, Pair<Long, Long>> entry : mpctTaskInfo.getAgentID2fileID().entrySet()) {
            if (agentMapper.selectById(entry.getKey()) == null) {
                throw new Exception("Agent不存在");
            }
            // TODO 检查app有权访问File
            // if (fileMapper.selectById(entry.getValue().getRight()) == null) {
            // throw new Exception("文件不存在");
            // }
        }
        mpcTaskMapper.insert(mpctTaskInfo);
        // TODO 不把其他方使用的数据发送给无关方
        // mpctTaskInfo.setNull();
        // mpctTaskInfo.setDataId(null);
        List<Mono<R<?>>> monos = new ArrayList<Mono<R<?>>>();
        for (Map.Entry<Long, Pair<Long, Long>> entry : mpctTaskInfo.getAgentID2fileID().entrySet()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(entry.getKey());
            mpcTaskAgent.setPart(entry.getValue().getLeft());
            mpcTaskAgent.setMpcTaskId(mpctTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpctTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
            monos.add(centerWebClientService.center2AgentWebClient(entry.getKey()).post().uri("/MpcTasks/create")
                    .bodyValue((UploadAgentTaskInfo) mpctTaskInfo).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<R<?>>() {
                    }));
        }
        Mono.when(monos).block();
        switch (mpctTaskInfo.getTaskType()) {
            case GARNET_MPC:
            default:
                mpcRun(mpctTaskInfo);
                break;

            case GARNET_PSI:
                psiRun(mpctTaskInfo);
                break;
        }
    }

    @Async("customExecutor")
    private void mpcRun(UploadAgentTaskInfo mpcTask) throws Exception {
        garnetService.compile(mpcTask);
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
        garnetService.run(mpcTask);
        mpcTaskOutputService.saveOutputFromInner(mpcTaskMapper.selectById(mpcTask.getUid()));
    }

    @Async("customExecutor")
    private void psiRun(UploadAgentTaskInfo mpcTask) throws Exception {
        garnetService.compile(mpcTask);
        garnetService.idExtract(inputMapper.selectById(mpcTask.getDataId()).getPath(), mpcTask.getUid(),
                mpcTask.getPart());
        while (ready(mpcTask.getUid()) == false) {
            Thread.sleep(1000);
        }
        if (run(mpcTask.getUid()) == false) {
            throw new Exception("任务未就绪");
        }
        garnetService.run(mpcTask);
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
}
