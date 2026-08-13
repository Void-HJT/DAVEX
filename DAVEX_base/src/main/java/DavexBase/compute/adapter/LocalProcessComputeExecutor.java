package DavexBase.compute.adapter;

import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;
import DavexBase.compute.port.ComputeExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 使用 ProcessBuilder 执行本地程序，并统一管理输出、退出码、超时和取消。
 */
public class LocalProcessComputeExecutor
        implements ComputeExecutor<LocalProcessCommand> {

    private static final Duration STOP_GRACE_PERIOD = Duration.ofMillis(500);

    private final Map<ExecutionId, RunningExecution> executions =
            new ConcurrentHashMap<>();

    /**
     * 启动前验证工作目录，避免在 ProcessBuilder 内产生难以识别的错误。
     */
    @Override
    public void prepare(LocalProcessCommand command) {
        if (!Files.isDirectory(command.workingDirectory())) {
            throw new IllegalArgumentException(
                    "工作目录不存在: " + command.workingDirectory());
        }
    }

    /**
     * 启动进程并异步读取输出，防止输出缓冲区填满后阻塞子进程。
     */
    @Override
    public ExecutionId start(LocalProcessCommand command) {
        prepare(command);

        try {
            Process process = new ProcessBuilder(command.arguments())
                    .directory(command.workingDirectory().toFile())
                    .start();

            ExecutionId executionId =
                    new ExecutionId(UUID.randomUUID().toString());

            // 输出流关闭可能由取消或超时触发，因此读取任务共享终止状态。
            AtomicBoolean cancelled = new AtomicBoolean(false);
            AtomicBoolean timedOut = new AtomicBoolean(false);

            RunningExecution execution = new RunningExecution(
                    process,
                    readAsync(process.getInputStream(), cancelled, timedOut),
                    readAsync(process.getErrorStream(), cancelled, timedOut),
                    cancelled,
                    timedOut);

            executions.put(executionId, execution);

            CompletableFuture.delayedExecutor(
                    command.timeout().toMillis(),
                    TimeUnit.MILLISECONDS)
                    .execute(() -> timeout(executionId));

            return executionId;
        } catch (IOException e) {
            throw new IllegalStateException("无法启动本地计算进程", e);
        }
    }

    /**
     * 只有进程实际结束后才返回终态，避免调用方过早收集结果。
     */
    @Override
    public ExecutionStatus status(ExecutionId executionId) {
        RunningExecution execution = requireExecution(executionId);

        if (execution.process().isAlive()) {
            return ExecutionStatus.RUNNING;
        }
        if (execution.timedOut().get()) {
            return ExecutionStatus.TIMED_OUT;
        }
        if (execution.cancelled().get()) {
            return ExecutionStatus.CANCELLED;
        }
        return execution.process().exitValue() == 0
                ? ExecutionStatus.SUCCEEDED
                : ExecutionStatus.FAILED;
    }

    /**
     * 主动取消正在运行的进程，并在正常退出失败时强制回收。
     */
    @Override
    public void cancel(ExecutionId executionId) {
        RunningExecution execution = requireExecution(executionId);

        if (!execution.process().isAlive()) {
            return;
        }

        execution.cancelled().set(true);
        terminate(execution.process());
    }

    /**
     * 收集已经结束的执行结果；运行中的进程不能提前收集。
     */
    @Override
    public ExecutionResult collect(ExecutionId executionId) {
        RunningExecution execution = requireExecution(executionId);

        if (execution.process().isAlive()) {
            throw new IllegalStateException("计算仍在运行，不能收集结果");
        }

        return new ExecutionResult(
                executionId,
                status(executionId),
                execution.process().exitValue(),
                execution.stdout().join(),
                execution.stderr().join());
    }

    /**
     * 超过命令指定时间后终止进程，并保留超时状态。
     */
    private void timeout(ExecutionId executionId) {
        RunningExecution execution = executions.get(executionId);

        if (execution == null
                || !execution.process().isAlive()
                || execution.cancelled().get()) {
            return;
        }

        execution.timedOut().set(true);
        terminate(execution.process());
    }

    /**
     * 先尝试正常关闭，超过宽限时间后再强制结束。
     */
    private void terminate(Process process) {
        process.destroy();
        try {
            if (!process.waitFor(
                    STOP_GRACE_PERIOD.toMillis(),
                    TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                process.waitFor();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IllegalStateException("等待进程结束时被中断", e);
        }
    }

    /**
     * 异步读取进程输出。
     * 取消或超时导致流关闭时保留已经读取的内容，不将其误判为执行失败。
     */
    private CompletableFuture<String> readAsync(
            InputStream stream,
            AtomicBoolean cancelled,
            AtomicBoolean timedOut) {

        return CompletableFuture.supplyAsync(() -> {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try (stream; output) {
                stream.transferTo(output);
                return output.toString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                if (cancelled.get() || timedOut.get()) {
                    return output.toString(StandardCharsets.UTF_8);
                }
                throw new IllegalStateException("读取进程输出失败", e);
            }
        });
    }

    private RunningExecution requireExecution(ExecutionId executionId) {
        RunningExecution execution = executions.get(executionId);
        if (execution == null) {
            throw new IllegalArgumentException(
                    "执行记录不存在: " + executionId.value());
        }
        return execution;
    }

    /**
     * 保存一次运行中的进程、输出读取任务及终止原因。
     */
    private record RunningExecution(
            Process process,
            CompletableFuture<String> stdout,
            CompletableFuture<String> stderr,
            AtomicBoolean cancelled,
            AtomicBoolean timedOut) {
    }
}