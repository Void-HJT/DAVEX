package DavexBase.compute.adapter;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectExecCmd;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 阶段 6.4.1 测试：验证 Docker exec 的统一状态、输出和容器内进程取消，
 * 不依赖测试服务器上必须存在真实 Garnet 容器。
 */
@ExtendWith(MockitoExtension.class)
class DockerComputeExecutorTest {

    @Mock
    private DockerClient dockerClient;
    @Mock
    private ExecCreateCmd createCmd;
    @Mock
    private ExecCreateCmdResponse createResponse;
    @Mock
    private ExecStartCmd startCmd;
    @Mock
    private InspectExecCmd inspectCmd;
    @Mock
    private InspectExecResponse inspectResponse;

    private DockerComputeExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new DockerComputeExecutor(dockerClient);
    }

    @Test
    void startsDockerExecAndCollectsOutput() {
        arrangeStart("docker-exec-1", "standard-output", "standard-error");
        when(dockerClient.inspectExecCmd("docker-exec-1")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(inspectResponse);
        when(inspectResponse.isRunning()).thenReturn(false);
        when(inspectResponse.getExitCode()).thenReturn(0);

        ExecutionId executionId = executor.start(command(Duration.ofSeconds(5)));
        ExecutionResult result = executor.collect(executionId);

        assertEquals("docker-exec-1", executionId.value());
        assertEquals(ExecutionStatus.SUCCEEDED, result.status());
        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("standard-output"));
        assertTrue(result.stderr().contains("standard-error"));
    }

    @Test
    void mapsNonZeroDockerExitCodeToFailed() {
        arrangeStart("docker-exec-2", "", "compile failed");
        when(dockerClient.inspectExecCmd("docker-exec-2")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(inspectResponse);
        when(inspectResponse.isRunning()).thenReturn(false);
        when(inspectResponse.getExitCode()).thenReturn(9);

        ExecutionId executionId = executor.start(command(Duration.ofSeconds(5)));

        assertEquals(ExecutionStatus.FAILED, executor.status(executionId));
        assertEquals(9, executor.collect(executionId).exitCode());
    }

    @Test
    void cancelsTheContainerProcessByDockerExecPid() throws Exception {
        ExecCreateCmd killCmd = org.mockito.Mockito.mock(ExecCreateCmd.class);
        ExecCreateCmdResponse killResponse = org.mockito.Mockito.mock(ExecCreateCmdResponse.class);
        ExecStartCmd killStartCmd = org.mockito.Mockito.mock(ExecStartCmd.class);

        arrangeStart("docker-exec-3", "", "");
        when(dockerClient.execCreateCmd("container-1")).thenReturn(createCmd, killCmd);
        when(dockerClient.inspectExecCmd("docker-exec-3")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(inspectResponse, inspectResponse);
        when(inspectResponse.isRunning()).thenReturn(true, false);
        when(inspectResponse.getPid()).thenReturn(42);

        when(killCmd.withAttachStdout(true)).thenReturn(killCmd);
        when(killCmd.withAttachStderr(true)).thenReturn(killCmd);
        when(killCmd.withCmd("kill", "-TERM", "42")).thenReturn(killCmd);
        when(killResponse.getId()).thenReturn("kill-exec");
        when(killCmd.exec()).thenReturn(killResponse);
        when(dockerClient.execStartCmd("kill-exec")).thenReturn(killStartCmd);
        when(killStartCmd.exec(any())).thenAnswer(invocation -> {
            ResultCallback<Frame> callback = invocation.getArgument(0);
            callback.onComplete();
            return callback;
        });

        ExecutionId executionId = executor.start(command(Duration.ofSeconds(5)));
        executor.cancel(executionId);

        verify(killCmd).withCmd("kill", "-TERM", "42");
        assertEquals(ExecutionStatus.CANCELLED, executor.status(executionId));
    }

    private void arrangeStart(String execId, String stdout, String stderr) {
        when(dockerClient.execCreateCmd("container-1")).thenReturn(createCmd);
        when(createCmd.withWorkingDir("/usr/src/Garnet")).thenReturn(createCmd);
        when(createCmd.withCmd("python3", "compile.py")).thenReturn(createCmd);
        when(createCmd.withAttachStdout(true)).thenReturn(createCmd);
        when(createCmd.withAttachStderr(true)).thenReturn(createCmd);
        when(createCmd.exec()).thenReturn(createResponse);
        when(createResponse.getId()).thenReturn(execId);
        when(dockerClient.execStartCmd(execId)).thenReturn(startCmd);
        when(startCmd.exec(any())).thenAnswer(invocation -> {
            ResultCallback<Frame> callback = invocation.getArgument(0);
            if (!stdout.isEmpty()) {
                callback.onNext(frame(StreamType.STDOUT, stdout));
            }
            if (!stderr.isEmpty()) {
                callback.onNext(frame(StreamType.STDERR, stderr));
            }
            callback.onComplete();
            return callback;
        });
    }

    private DockerComputeCommand command(Duration timeout) {
        return new DockerComputeCommand(
                "TASK-1",
                timeout,
                "container-1",
                "/usr/src/Garnet",
                List.of("python3", "compile.py"));
    }

    private Frame frame(StreamType type, String text) {
        return new Frame(type, text.getBytes(StandardCharsets.UTF_8));
    }
}
