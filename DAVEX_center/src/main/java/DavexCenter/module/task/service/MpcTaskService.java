package DavexCenter.module.task.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.My;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.programs.GarnetService;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import DavexBase.service.notification.NotificationService;
import DavexBase.compute.garnet.GarnetComputeAdapter;

import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.task.assembler.AgentMpcTaskRequestProjector;
import DavexCenter.module.task.assembler.MpcTaskCommandAssembler;
import DavexCenter.module.task.port.AgentFileMetadataClient;
import DavexCenter.module.task.port.AgentMpcTaskClient;

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

    /**
     * 统一管理Garnet编译、运行、状态和取消。
     */
    @Autowired
    private GarnetComputeAdapter garnetComputeAdapter;

    @Autowired
    private MpcTaskOutputService mpcTaskOutputService;

    @Autowired
    private AgentMpcTaskClient agentMpcTaskClient;

    @Autowired
    private AgentFileMetadataClient agentFileMetadataClient;

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

    public MpcTask create(MpcTaskCommand command) throws Exception {
        if (!my.getId().equals(command.centerId())) {
            throw new Exception("发送错误");
        }

        for (ParticipantInput participant : command.participants()) {
            if (agentMapper.selectById(participant.agentId()) == null) {
                throw new Exception("Agent不存在");
            }
        }

        command = parameterUpdate(command);

        MpcTask mpcTask = MpcTaskCommandAssembler.toEntity(command);
        mpcTaskMapper.insert(mpcTask);

        // 数据库生成 UID 后同步回不可变命令，供参与方记录和 Agent 请求使用。
        command = command.withUid(mpcTask.getUid());

        for (ParticipantInput participant : command.participants()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(participant.agentId());
            mpcTaskAgent.setPart(participant.part());
            mpcTaskAgent.setMpcTaskId(command.uid());
            mpcTaskAgent.setCenterId(command.centerId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);

            agentMpcTaskClient.createTask(participant.agentId(),
                    AgentMpcTaskRequestProjector.toRequest(command, participant));
        }

        return mpcTask;
    }

    @Async("customExecutor")
    public void mpcRun(MpcTask mpcTask) throws Exception {
        try {
            garnetComputeAdapter.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("MPC任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "MPC任务编译失败", errorContent, mpcTask.getUid(), 0,
                    "mpc");
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
            garnetComputeAdapter.run(mpcTask);
        } catch (Exception e) {
            // 运行失败的通知
            String errorContent = String.format("MPC任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "MPC任务运行失败", errorContent, mpcTask.getUid(), 0,
                    "mpc");
            throw e;
        }
        mpcTaskOutputService.saveOutputFromInner(mpcTaskMapper.selectById(mpcTask.getUid()));
    }

    @Async("customExecutor")
    public void psiRun(MpcTask mpcTask) throws Exception {
        try {
            garnetComputeAdapter.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("PSI任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "PSI任务编译失败", errorContent, mpcTask.getUid(), 0,
                    "psi");
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
            garnetComputeAdapter.run(mpcTask);
        } catch (Exception e) {
            // 运行失败的通知
            String errorContent = String.format("PSI任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            notificationService.setMessage(mpcTask.getApplicationId(), "PSI任务运行失败", errorContent, mpcTask.getUid(), 0,
                    "psi");
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
        // 通过通信端口检查每个参与 Agent 的任务准备状态。
        for (MpcTaskAgent agent : agents) {
            if (!agentMpcTaskClient.isReady(
                    agent.getAgentId(), mpcTaskId)) {
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

        for (MpcTaskAgent agent : agents) {
            if (!agentMpcTaskClient.runTask(
                    agent.getAgentId(), mpcTaskId)) {
                return false;
            }
        }

        return true;
    }

    public List<MpcTask> list() {
        return mpcTaskMapper.selectList(null);
    }

    public MpcTaskCommand parameterUpdate(MpcTaskCommand command) {

        if (command.taskType() != MpcTaskCommand.TaskType.GARNET_PSI) {
            return command;
        }

        Map<String, Object> compileParameters = new LinkedHashMap<>();

        Long p0Data = garnetService.csvCount(
                inputMapper.selectById(command.dataId()).getPath());

        ParticipantInput participant = command.participants().get(0);

        try {
            // 通过文件元数据端口查询 Agent 输入文件的行数。
            Long p1Data = agentFileMetadataClient.getRowCount(
                    participant.agentId(),
                    participant.fileId());

            compileParameters.put("P0_Data", p0Data);

            // Agent 返回的 CSV 行数包含表头，因此实际数据行数减一。
            compileParameters.put("P1_Data", p1Data - 1);
        } catch (Exception e) {
            e.printStackTrace();
            return command;
        }

        TaskOptions currentOptions = command.options();
        Map<String, Object> runtimeParameters = currentOptions == null
                ? null
                : currentOptions.runtimeParameters();

        return command.withOptions(
                new TaskOptions(
                        compileParameters,
                        runtimeParameters));
    }
}
