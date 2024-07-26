package DveCenter.module.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.R;
import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveAgent.mapper.AgentMapper;
import DveAgent.mapper.FileMapper;
import DveAgent.mapper.MpcTaskAgentMapper;
import DveAgent.mapper.MpcTaskMapper;
import DveCenter.common.My;
import DveCenter.entity.Input;
import DveCenter.info.UploadCenterTaskInfo;
import DveCenter.mapper.InputMapper;
import DveCenter.module.auth.service.CenterWebClientService;
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
    private FileMapper fileMapper;

    @Autowired
    private My my;

    private CenterWebClientService centerWebClientService;

    public Input createInput(Input input) {
        inputMapper.insert(input);
        return input;
    }

    public void addAgentToMPCTask(Long mpcTaskId, Long centerId, List<MpcTaskAgent> agents) {
        // 检查List的大小 是否与mpcTaskId对应的MpcTask的相等
        // int size = agents.size();
        // if(size != mpcTaskMapper.getMpcTaskById(mpcTaskId).get().getPn()){
        // throw new RuntimeException("The number of agents is not equal to the number
        // of agents required by the task");
        // }

        // 将agents插入到MpcTaskAgent表
        // 检测重复 对重复的处理逻辑：直接按照mpcTaskId 和 centerId 把表项全部删掉 然后重新插入
        LambdaQueryWrapper<MpcTaskAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MpcTaskAgent::getMpcTaskId, mpcTaskId)
                .eq(MpcTaskAgent::getCenterId, centerId);
        // 执行删除操作
        mpcTaskAgentMapper.delete(queryWrapper);
        for (MpcTaskAgent agent : agents) {
            mpcTaskAgentMapper.insert(agent);
        }

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

    public void create(UploadCenterTaskInfo mpctTaskInfo) throws Exception {
        if (mpctTaskInfo.getCenterId() != my.getId()) {
            throw new Exception("发送错误");
        }

        if (mpcTaskMapper.selectById(mpctTaskInfo.getUid()) != null) {
            throw new Exception("任务已存在");
        }

        for (Map.Entry<Long, Pair<Long, Long>> entry : mpctTaskInfo.getAgentID2fileID().entrySet()) {
            if (agentMapper.selectById(entry.getKey()) == null) {
                throw new Exception("Agent不存在");
            }
            // TODO 查询File需要修改，使用AgentID
            // TODO 检查app有权访问File
            if (fileMapper.selectById(entry.getValue().getRight()) == null) {
                throw new Exception("文件不存在");
            }
        }

        UploadCenterTaskInfo transInfo = mpctTaskInfo;
        transInfo.setData(null);
        List<Mono<R<?>>> monos = new ArrayList<Mono<R<?>>>();
        for (Map.Entry<Long, Pair<Long, Long>> entry : mpctTaskInfo.getAgentID2fileID().entrySet()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(entry.getKey());
            mpcTaskAgent.setFileId(entry.getValue().getRight());
            mpcTaskAgent.setPart(entry.getValue().getLeft());
            mpcTaskAgent.setMpcTaskId(mpctTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpctTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
            monos.add(centerWebClientService.center2AgentWebClient(entry.getKey()).post().uri("/MpcTasks/create")
                    .bodyValue(transInfo).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<R<?>>() {
                    }));
        }
        Mono.when(monos).block();
        mpcTaskMapper.insert(mpctTaskInfo);
    }

    // TODO 检查本机是否就绪
    public Boolean ready(Long mpcTaskId) throws Exception {
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
                        .uri(uriBuilder -> uriBuilder.path("/MpcTasks/ready/{mpcTaskId}")
                                .build(mpcTaskId))
                        .retrieve().bodyToMono(new ParameterizedTypeReference<R<Boolean>>() {
                        });
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).collectList().block();
        for (R<Boolean> response : responses) {
            if (!response.getBody().getData()) {
                return false;
            }
        }
        return true;
    }
}
