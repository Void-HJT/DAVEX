
package DavexAgent.module.task.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import DavexBase.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.entity.MpcTaskOutput;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.directory.FileFolderService;
import DavexBase.service.programs.GarnetService;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.compute.garnet.GarnetComputeAdapter;

import DavexAgent.module.task.assembler.MpcTaskCommandAssembler;
import DavexAgent.module.task.port.CenterMpcResultClient;
import DavexAgent.module.task.port.CenterTaskNotificationClient;

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

    /**
     * 统一管理Garnet编译、运行、状态和取消。
     */
    @Autowired
    private GarnetComputeAdapter garnetComputeAdapter;

    @Autowired
    MpcMapper mpcMapper;

    @Autowired
    MpcService mpcService;

    @Autowired
    FileMapper fileMapper;

    @Autowired
    FileFolderService fileFolderService;

    @Autowired
    private CenterTaskNotificationClient notificationClient;

    @Autowired
    private CenterMpcResultClient resultClient;

    // TODO 检查File权限
    public void createMpcTask(MpcTaskCommand command) throws Exception {

        if (command.uid() != null
                && mpcTaskMapper.selectById(command.uid()) != null) {
            throw new Exception("任务已存在");
        }

        for (ParticipantInput participant : command.participants()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(participant.agentId());
            mpcTaskAgent.setPart(participant.part());
            mpcTaskAgent.setMpcTaskId(command.uid());
            mpcTaskAgent.setCenterId(command.centerId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
        }

        if (mpcMapper.selectById(command.mpcId()) == null) {
            mpcService.downloadMPC(command.centerId(), command.mpcId());
        }

        MpcTask mpcTask = MpcTaskCommandAssembler.toEntity(command);
        mpcTaskMapper.insert(mpcTask);

        switch (command.taskType()) {
            case GARNET_MPC:
            default:
                preprocess(mpcTask);
                break;
            case GARNET_PSI:
                psiPreprocess(mpcTask);
                break;
        }
    }

    /**
     * 编译 MPC 程序并准备当前 Agent 的任务输入文件。
     *
     * 编译失败时向 Center 发送失败通知，然后继续抛出原异常，
     * 由上层业务入口按原有错误语义处理。
     *
     * @param mpcTask 已完成持久化装配的 MPC 任务实体
     */
    @Async("customExecutor")
    public void preprocess(MpcTask mpcTask) throws Exception {
        try {
            garnetComputeAdapter.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("MPC任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            // 通过通信端口向任务所属 Center 回传 MPC 编译失败状态。
            notificationClient.sendNotification(
                    mpcTask.getCenterId(), mpcTask.getApplicationId(),
                    "MPC任务编译失败", errorContent, mpcTask.getUid(), 0, "mpc");
            throw e;
        }
        garnetService.link(fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getUid(),
                mpcTask.getPart());
    }

    @Async("customExecutor")
    private void psiPreprocess(MpcTask mpcTask) throws Exception {
        try {
            garnetComputeAdapter.compile(mpcTask);
        } catch (Exception e) {
            // 编译失败的通知
            String errorContent = String.format("PSI任务编译失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            // 通过通信端口向任务所属 Center 回传 PSI 编译失败状态。
            notificationClient.sendNotification(
                    mpcTask.getCenterId(), mpcTask.getApplicationId(),
                    "PSI任务编译失败", errorContent, mpcTask.getUid(), 0, "psi");
            throw e;
        }
        garnetService.csvExtract(
                fileFolderService.getFilePath(fileMapper.selectById(mpcTask.getDataId()), my.getBase_path()),
                mpcTask.getRuntimeParameters().getString("PK"), mpcTask.getUid(), mpcTask.getPart());
    }

    @Async("customExecutor")
    public void run(MpcTask mpcTask) throws Exception {
        try {
            garnetComputeAdapter.run(mpcTask);
        } catch (Exception e) {
            // 运行失败的通知
            String errorContent = String.format("MPC任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            // 通过通信端口向任务所属 Center 回传 MPC 运行失败状态。
            notificationClient.sendNotification(
                    mpcTask.getCenterId(), mpcTask.getApplicationId(),
                    "MPC任务运行失败", errorContent, mpcTask.getUid(), 0, "mpc");
            throw e;
        }
        // 普通 MPC 的结果保留在各参与节点，不需要上传到 Center。
    }

    @Async("customExecutor")
    public void psiRun(MpcTask mpcTask) throws Exception {
        try {
            garnetComputeAdapter.run(mpcTask);
        } catch (Exception e) {
            // 任务执行失败的通知
            String errorContent = String.format("PSI任务运行失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            // 通过通信端口向任务所属 Center 回传 PSI 运行失败状态。
            notificationClient.sendNotification(
                    mpcTask.getCenterId(), mpcTask.getApplicationId(),
                    "PSI任务运行失败", errorContent, mpcTask.getUid(), 0, "psi");
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
            // Multipart 构造和响应解析由结果通信适配器负责。
            resultClient.uploadPsiResult(mpcTask.getCenterId(), filePath, mpcTaskOutput);

            // 成功保存结果的消息通知
            String content = String.format("PSI任务结果保存成功\n任务ID: %s\n任务类型: %s\n运行结果: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), mpcTask.getStatus());
            Integer code = 1;

            // 通过通信端口回传 PSI 结果保存成功状态。
            notificationClient.sendNotification(
                    mpcTask.getCenterId(), mpcTask.getApplicationId(),
                    "PSI任务运行结束", content, mpcTask.getUid(), code, "psi");
        } catch (Exception e) {
            e.printStackTrace();

            // 保存失败消息通知
            String content = String.format("PSI任务结果保存失败\n任务ID: %s\n任务类型: %s\n错误信息: %s",
                    mpcTask.getUid(), mpcTask.getTaskType(), e.getMessage());
            Integer code = 0;

            // 通过通信端口回传 PSI 结果保存失败状态。
            notificationClient.sendNotification(
                    mpcTask.getCenterId(), mpcTask.getApplicationId(),
                    "PSI任务运行结束", content, mpcTask.getUid(), code, "psi");
        }
    }

    public Boolean ready(String mpcTaskId) throws Exception {
        return mpcTaskMapper.selectById(mpcTaskId).getStatus() == MpcTask.Status.READY;
    }
}
