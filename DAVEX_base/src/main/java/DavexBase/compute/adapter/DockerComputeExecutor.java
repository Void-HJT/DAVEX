package DavexBase.compute.adapter;

import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;
import DavexBase.compute.port.ComputeExecutor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 使用docker-java管理容器内命令的启动、状态、输出、超时和取消。
 */
@Component
public class DockerComputeExecutor implements ComputeExecutor<DockerComputeCommand> {

    private static final Duration STOP_GRACE_PERIOD = Duration.ofMillis(500);

    private final DockerClient dockerClient;
    private final Map<ExecutionId, RunningExecution> executions = new ConcurrentHashMap<>();

    public DockerComputeExecutor() {
        this(DockerClientBuilder.getInstance().build());
    }

    /**
     * 保留客户端注入构造函数，方便单元测试隔离Docker环境。
     */
    public DockerComputeExecutor(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public void prepare(DockerComputeCommand command) {
        // 命令自身已经完成必要参数校验。
    }

    /**
     * 创建并异步启动Docker exec，使用Docker exec ID作为统一执行ID。
     */
    @Override
    public ExecutionId start(DockerComputeCommand command) {
        prepare(command);

        var created = dockerClient.execCreateCmd(command.containerId())
                .withWorkingDir(command.workingDirectory())
                .withCmd(command.arguments().toArray(new String[0]))
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        DockerOutputCallback callback = new DockerOutputCallback();
        dockerClient.execStartCmd(created.getId()).exec(callback);

        ExecutionId executionId = new ExecutionId(created.getId());
        executions.put(
                executionId,
                new RunningExecution(
                        command.containerId(),
                        callback,
                        new AtomicBoolean(false),
                        new AtomicBoolean(false)));

        CompletableFuture.delayedExecutor(
                command.timeout().toMillis(),
                TimeUnit.MILLISECONDS)
                .execute(() -> timeout(executionId));

        return executionId;
    }

    /**
     * 只有Docker确认命令结束后才返回终态。
     * 与取消和超时处理使用同一执行记录锁，避免观察到中间状态。
     */
    @Override
    public ExecutionStatus status(ExecutionId executionId) {
        RunningExecution execution = requireExecution(executionId);

        synchronized (execution) {
            InspectExecResponse response = dockerClient.inspectExecCmd(executionId.value()).exec();

            if (Boolean.TRUE.equals(response.isRunning())) {
                return ExecutionStatus.RUNNING;
            }
            if (execution.timedOut().get()) {
                return ExecutionStatus.TIMED_OUT;
            }
            if (execution.cancelled().get()) {
                return ExecutionStatus.CANCELLED;
            }

            Integer exitCode = response.getExitCode();
            return exitCode != null && exitCode == 0
                    ? ExecutionStatus.SUCCEEDED
                    : ExecutionStatus.FAILED;
        }
    }

    /**
     * 根据Docker exec PID终止容器内进程，而不是只取消Java线程。
     * 终止和取消标记在同一临界区完成，避免短暂暴露FAILED状态。
     */
    @Override
    public void cancel(ExecutionId executionId) {
        RunningExecution execution = requireExecution(executionId);

        synchronized (execution) {
            if (terminateContainerProcess(
                    executionId,
                    execution.containerId())) {
                execution.cancelled().set(true);
            }
        }
    }

    /**
     * 等待输出回调结束并收集退出码和标准输出。
     */
    @Override
    public ExecutionResult collect(ExecutionId executionId) {
        RunningExecution execution = requireExecution(executionId);
        ExecutionStatus finalStatus = status(executionId);

        if (finalStatus == ExecutionStatus.RUNNING) {
            throw new IllegalStateException("Docker命令仍在运行");
        }

        try {
            if (!execution.callback().awaitCompletion(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("等待Docker输出结束超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待Docker输出时被中断", e);
        }

        InspectExecResponse response = dockerClient.inspectExecCmd(executionId.value()).exec();

        return new ExecutionResult(
                executionId,
                finalStatus,
                response.getExitCode(),
                execution.callback().stdout(),
                execution.callback().stderr());
    }

    /**
     * 超时后终止容器内进程，并在实际执行终止后记录超时状态。
     * 超时终止和状态标记在同一临界区完成。
     */
    private void timeout(ExecutionId executionId) {
        RunningExecution execution = executions.get(executionId);

        if (execution == null) {
            return;
        }

        synchronized (execution) {
            if (terminateContainerProcess(
                    executionId,
                    execution.containerId())) {
                execution.timedOut().set(true);
            }
        }
    }

    /**
     * 先发送TERM，宽限期内未退出再发送KILL。
     */
    private boolean terminateContainerProcess(
            ExecutionId executionId,
            String containerId) {

        InspectExecResponse response = dockerClient.inspectExecCmd(executionId.value()).exec();

        if (!Boolean.TRUE.equals(response.isRunning())) {
            return false;
        }

        Integer pid = response.getPid();
        if (pid == null || pid <= 0) {
            throw new IllegalStateException(
                    "无法取得Docker exec进程PID: " + executionId.value());
        }

        sendSignal(containerId, "TERM", pid);

        if (waitUntilStopped(executionId, STOP_GRACE_PERIOD)) {
            return true;
        }

        sendSignal(containerId, "KILL", pid);

        if (!waitUntilStopped(executionId, STOP_GRACE_PERIOD)) {
            throw new IllegalStateException(
                    "Docker exec进程无法终止: " + executionId.value());
        }

        return true;
    }

    /**
     * 通过一个独立的Docker exec向目标PID发送信号。
     */
    private void sendSignal(
            String containerId,
            String signal,
            Integer pid) {

        var created = dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("kill", "-" + signal, pid.toString())
                .exec();

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>();

        try {
            dockerClient.execStartCmd(created.getId())
                    .exec(callback)
                    .awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "发送Docker终止信号时被中断", e);
        }
    }

    private boolean waitUntilStopped(
            ExecutionId executionId,
            Duration timeout) {

        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            InspectExecResponse response = dockerClient.inspectExecCmd(executionId.value()).exec();

            if (!Boolean.TRUE.equals(response.isRunning())) {
                return true;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "等待Docker进程结束时被中断", e);
            }
        }

        return false;
    }

    private RunningExecution requireExecution(ExecutionId executionId) {
        RunningExecution execution = executions.get(executionId);
        if (execution == null) {
            throw new IllegalArgumentException(
                    "Docker执行记录不存在: " + executionId.value());
        }
        return execution;
    }

    /**
     * 保存一次Docker执行的容器、输出和终止原因。
     */
    private record RunningExecution(
            String containerId,
            DockerOutputCallback callback,
            AtomicBoolean cancelled,
            AtomicBoolean timedOut) {
    }

    /**
     * 分别收集Docker命令的标准输出和错误输出。
     */
    private static final class DockerOutputCallback
            extends ResultCallback.Adapter<Frame> {

        private final StringBuilder stdout = new StringBuilder();
        private final StringBuilder stderr = new StringBuilder();

        @Override
        public synchronized void onNext(Frame frame) {
            if (frame == null || frame.getPayload() == null) {
                return;
            }

            String text = new String(
                    frame.getPayload(),
                    StandardCharsets.UTF_8);

            switch (frame.getStreamType()) {
                case STDOUT -> stdout.append(text);
                case STDERR -> stderr.append(text);
                default -> {
                    // 忽略未使用的Docker流类型。
                }
            }
        }

        private synchronized String stdout() {
            return stdout.toString();
        }

        private synchronized String stderr() {
            return stderr.toString();
        }
    }
}