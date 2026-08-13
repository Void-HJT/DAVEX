package DavexBase.compute.garnet;

import DavexBase.common.My;
import DavexBase.compute.adapter.DockerComputeCommand;
import DavexBase.compute.adapter.DockerComputeExecutor;
import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.properties.GarnetProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一编排Garnet程序准备、Docker执行、任务状态和取消操作。
 * Center与Agent业务Service不再直接管理Garnet命令生命周期。
 */
@Component
@ConditionalOnProperty(name = "garnet.enabled", havingValue = "true")
public class GarnetComputeAdapter {

    private static final Duration POLL_INTERVAL = Duration.ofMillis(50);

    private final GarnetComputeCommandFactory commandFactory;
    private final DockerComputeExecutor executor;
    private final MpcMapper mpcMapper;
    private final MpcTaskMapper taskMapper;
    private final My my;
    private final GarnetProperties properties;

    /**
     * 保存任务当前对应的Docker执行ID，供取消操作使用。
     */
    private final Map<String, ExecutionId> activeExecutions = new ConcurrentHashMap<>();

    public GarnetComputeAdapter(
            GarnetComputeCommandFactory commandFactory,
            DockerComputeExecutor executor,
            MpcMapper mpcMapper,
            MpcTaskMapper taskMapper,
            My my,
            GarnetProperties properties) {
        this.commandFactory = commandFactory;
        this.executor = executor;
        this.mpcMapper = mpcMapper;
        this.taskMapper = taskMapper;
        this.my = my;
        this.properties = properties;
    }

    /**
     * 准备MPC程序并执行Garnet编译，成功后保存生成的mpcName。
     */
    public void compile(MpcTask task) throws Exception {
        task.setStatus(MpcTask.Status.COMPILING);
        taskMapper.updateById(task);

        try {
            Mpc mpc = mpcMapper.selectById(task.getMpcId());
            if (mpc == null) {
                throw new IllegalArgumentException(
                        "MPC程序不存在: " + task.getMpcId());
            }

            prepareProgram(mpc);

            GarnetPreparedCommand prepared = commandFactory.compile(task, mpc);

            execute(task.getUid(), prepared.command());

            task.setMpcName(prepared.mpcName());
            task.setStatus(MpcTask.Status.READY);
            taskMapper.updateById(task);
        } catch (Exception e) {
            markFailed(task);
            throw e;
        }
    }

    /**
     * 执行已经完成编译和输入准备的Garnet任务。
     */
    public void run(MpcTask task) throws Exception {
        task.setStatus(MpcTask.Status.RUNNING);
        taskMapper.updateById(task);

        try {
            execute(task.getUid(), commandFactory.run(task));

            task.setStatus(MpcTask.Status.FINISHED);
            taskMapper.updateById(task);
        } catch (Exception e) {
            markFailed(task);
            throw e;
        }
    }

    /**
     * 根据业务任务ID取消当前Docker执行。
     */
    public void cancel(String taskId) {
        ExecutionId executionId = activeExecutions.get(taskId);
        if (executionId == null) {
            throw new IllegalArgumentException(
                    "任务没有正在执行的Garnet进程: " + taskId);
        }

        executor.cancel(executionId);
    }

    /**
     * 将MPC源程序安全复制到Garnet容器挂载目录。
     */
    private void prepareProgram(Mpc mpc) throws Exception {
        if (mpc.getPath() == null || mpc.getPath().isBlank()) {
            throw new IllegalArgumentException("MPC程序路径不能为空");
        }

        Path dataRoot = Path.of(my.getBase_path())
                .toAbsolutePath()
                .normalize();
        Path source = dataRoot.resolve(mpc.getPath())
                .normalize();

        if (!source.startsWith(dataRoot)) {
            throw new IllegalArgumentException(
                    "MPC程序路径越界: " + mpc.getPath());
        }
        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException(
                    "MPC程序文件不存在: " + source);
        }

        Path mountRoot = Path.of(properties.getMpcPath())
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(mountRoot);

        Path target = mountRoot.resolve(source.getFileName())
                .normalize();

        if (!target.startsWith(mountRoot)) {
            throw new IllegalArgumentException(
                    "Garnet挂载路径越界");
        }

        if (!Files.exists(target)) {
            Files.copy(source, target);
        }
    }

    /**
     * 阻塞等待统一执行器返回终态，并保留Docker错误输出。
     */
    private ExecutionResult execute(
            String taskId,
            DockerComputeCommand command) throws Exception {

        ExecutionId executionId = executor.start(command);
        activeExecutions.put(taskId, executionId);

        try {
            ExecutionStatus status = executor.status(executionId);

            while (status == ExecutionStatus.RUNNING) {
                try {
                    Thread.sleep(POLL_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    executor.cancel(executionId);
                    throw new IllegalStateException(
                            "等待Garnet执行时被中断", e);
                }

                status = executor.status(executionId);
            }

            ExecutionResult result = executor.collect(executionId);

            if (result.status() != ExecutionStatus.SUCCEEDED) {
                throw new IllegalStateException(
                        failureMessage(result));
            }

            return result;
        } finally {
            activeExecutions.remove(taskId, executionId);
        }
    }

    private String failureMessage(ExecutionResult result) {
        if (result.stderr() != null
                && !result.stderr().isBlank()) {
            return result.stderr();
        }

        return "Garnet执行失败，状态: "
                + result.status()
                + "，退出码: "
                + result.exitCode();
    }

    private void markFailed(MpcTask task) {
        task.setStatus(MpcTask.Status.FAILED);
        taskMapper.updateById(task);
    }
}