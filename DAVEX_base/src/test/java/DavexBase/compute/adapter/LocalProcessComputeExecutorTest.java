package DavexBase.compute.adapter;

import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 6.3 测试：使用真实 Java 子进程验证成功、失败、超时和主动取消，
 * 确保业务层不需要直接操作 ProcessBuilder 与 Process。
 */
class LocalProcessComputeExecutorTest {

    @TempDir
    Path workingDirectory;

    private final LocalProcessComputeExecutor executor = new LocalProcessComputeExecutor();

    @Test
    void collectsSuccessfulProcessOutput() throws Exception {
        ExecutionId executionId = executor.start(command("success", Duration.ofSeconds(5)));

        awaitCompletion(executionId);
        ExecutionResult result = executor.collect(executionId);

        assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("process-output"));
        assertTrue(result.stderr().contains("process-warning"));
    }

    @Test
    void mapsNonZeroExitCodeToFailed() throws Exception {
        ExecutionId executionId = executor.start(command("failure", Duration.ofSeconds(5)));

        awaitCompletion(executionId);
        ExecutionResult result = executor.collect(executionId);

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertEquals(7, result.exitCode());
        assertTrue(result.stderr().contains("process-failure"));
    }

    @Test
    void terminatesProcessAfterTimeout() throws Exception {
        ExecutionId executionId = executor.start(command("sleep", Duration.ofMillis(100)));

        awaitCompletion(executionId);
        ExecutionResult result = executor.collect(executionId);

        assertEquals(ExecutionStatus.TIMED_OUT, result.status());
    }

    @Test
    void cancelsRunningProcess() throws Exception {
        ExecutionId executionId = executor.start(command("sleep", Duration.ofSeconds(5)));

        executor.cancel(executionId);
        awaitCompletion(executionId);
        ExecutionResult result = executor.collect(executionId);

        assertEquals(ExecutionStatus.CANCELLED, result.status());
    }

    private LocalProcessCommand command(String mode, Duration timeout) {
        String javaExecutable = Path.of(
                System.getProperty("java.home"), "bin", "java").toString();
        List<String> arguments = new ArrayList<>();
        arguments.add(javaExecutable);
        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path"));
        arguments.add(TestProgram.class.getName());
        arguments.add(mode);
        return new LocalProcessCommand("TASK-1", timeout, arguments, workingDirectory);
    }

    private void awaitCompletion(ExecutionId executionId) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(8).toNanos();
        while (executor.status(executionId) == ExecutionStatus.RUNNING
                && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(executor.status(executionId) != ExecutionStatus.RUNNING,
                "子进程必须在测试超时前退出或被回收");
    }

    /** 由测试启动的独立 Java 子进程。 */
    public static final class TestProgram {

        private TestProgram() {
        }

        public static void main(String[] args) throws Exception {
            switch (args[0]) {
                case "success" -> {
                    System.out.println("process-output");
                    System.err.println("process-warning");
                }
                case "failure" -> {
                    System.err.println("process-failure");
                    System.exit(7);
                }
                case "sleep" -> Thread.sleep(5_000);
                default -> throw new IllegalArgumentException("unknown mode");
            }
        }
    }
}
