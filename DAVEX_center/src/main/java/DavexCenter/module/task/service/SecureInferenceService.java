package DavexCenter.module.task.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.info.InferenceInfo;
import DavexBase.info.Parameter;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;

import DavexCenter.module.task.assembler.AgentMpcTaskRequestProjector;
import DavexCenter.module.task.assembler.MpcTaskCommandAssembler;
import DavexCenter.module.task.port.AgentSecureInferenceClient;

@Service
@ConditionalOnProperty(name = "garnet.enabled", havingValue = "true")
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
    private AgentSecureInferenceClient inferenceClient;
    private static final Logger logger = LoggerFactory.getLogger(SecureInferenceService.class);

    public MpcTask create(MpcTaskCommand command) throws Exception {
        if (command.taskType() != MpcTaskCommand.TaskType.GARNET_INFERENCE) {
            throw new Exception("任务类型不匹配");
        }

        if (!my.getId().equals(command.centerId())) {
            throw new Exception("发送错误");
        }

        for (ParticipantInput participant : command.participants()) {
            if (agentMapper.selectById(participant.agentId()) == null) {
                throw new Exception("Agent不存在");
            }
        }

        MpcTask mpcTask = MpcTaskCommandAssembler.toEntity(command);
        mpcTaskMapper.insert(mpcTask);

        // 数据库生成 UID 后同步回命令。
        command = command.withUid(mpcTask.getUid());

        // 收集任务后由通信适配器并发发送，业务层不再操作 WebClient。
        List<AgentSecureInferenceClient.TaskDispatch> dispatches = new ArrayList<>();

        for (ParticipantInput participant : command.participants()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(participant.agentId());
            mpcTaskAgent.setPart(participant.part());
            mpcTaskAgent.setMpcTaskId(command.uid());
            mpcTaskAgent.setCenterId(command.centerId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);

            dispatches.add(new AgentSecureInferenceClient.TaskDispatch(
                    participant.agentId(),
                    AgentMpcTaskRequestProjector.toRequest(
                            command,
                            participant)));
        }

        inferenceClient.createTasks(dispatches);
        return mpcTask;
    }

    /**
     * 获取安全推理程序，并在本地尚未登记时保存文件和元数据。
     */
    @Async("customExecutor")
    public Mpc downloadMPC(String agentId, String fileId) throws Exception {

        AgentSecureInferenceClient.Artifact artifact = inferenceClient.fetchArtifact(agentId, fileId);
        Mpc mpc = artifact.metadata();

        if (mpcMapper.selectById(mpc.getUid()) != null) {
            logger.info("MPC文件：{} 已存在", mpc.getUid());
            return mpc;
        }

        logger.info("开始下载mpc文件：{}", mpc.getUid());

        Path programsDirectory = Paths.get(my.getBase_path()).resolve("programs");
        Files.createDirectories(programsDirectory);

        Path filePath = Utils.resolveFileNameConflict(
                programsDirectory.resolve(artifact.fileName()));
        Files.write(filePath, artifact.content());

        mpc.setPath(Paths.get("programs")
                .resolve(filePath.getFileName())
                .toString());
        mpcMapper.insert(mpc);

        return mpc;
    }

    @Async("customExecutor")
    public void run(MpcTask mpcTask) throws Exception {
        mpcTaskService.mpcRun(mpcTask);
    }

    public MpcTaskCommand wrapMpcTaskCommand(
            InferenceInfo info,
            String dataId) throws Exception {

        logger.info("接收安全推理任务：{}", info);

        Mpc downloadedMpc = downloadMPC(info.getAgentId(), info.getFileId());
        Mpc storedMpc = mpcMapper.selectById(downloadedMpc.getUid());

        return new MpcTaskCommand(
                null,
                info.getApplicationId(),
                my.getId(),
                downloadedMpc.getUid(),
                resolveOptions(storedMpc, info),
                2,
                0L,
                my.getIp(),
                6099,
                dataId,
                MpcTaskCommand.TaskType.GARNET_INFERENCE,
                null,
                List.of(new ParticipantInput(
                        info.getAgentId(),
                        1L,
                        info.getFileId())));
    }

    /**
     * 生成本次推理任务使用的参数。
     *
     * 旧客户端未传参数时继续使用 MPC 注册默认值；新客户端传入的值只覆盖
     * 本次任务，不修改 MPC 注册信息，避免一次推理影响后续任务。
     */
    private TaskOptions resolveOptions(
            Mpc mpc,
            InferenceInfo info) throws Exception {

        if (mpc == null) {
            throw new Exception("Mpc is null");
        }

        Map<String, Object> compileParameters = defaultParameters(mpc.getCompileParameters());
        Map<String, Object> runtimeParameters = defaultParameters(mpc.getRuntimeParameters());

        if (info.getCompileParameters() != null) {
            compileParameters.putAll(info.getCompileParameters());
        }

        if (info.getRuntimeParameters() != null) {
            runtimeParameters.putAll(info.getRuntimeParameters());
        }

        return new TaskOptions(
                compileParameters,
                runtimeParameters);
    }

    private Map<String, Object> defaultParameters(List<Parameter> parameters) {

        Map<String, Object> defaults = new LinkedHashMap<>();

        for (Parameter parameter : parameters) {
            defaults.put(
                    parameter.getName(),
                    parameter.getDefaultValue());
        }

        return defaults;
    }
}
