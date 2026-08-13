package DavexBase.compute.garnet;

import DavexBase.compute.adapter.DockerComputeCommand;
import DavexBase.compute.adapter.DockerComputeExecutor;
import DavexBase.common.My;
import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.properties.GarnetProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 阶段 6.4.3 测试：验证Garnet适配器统一维护任务状态、执行结果和取消映射，
 * 业务Service只负责文件准备、节点协作及失败通知。
 */
@ExtendWith(MockitoExtension.class)
class GarnetComputeAdapterTest {

    @TempDir
    Path temporaryDirectory;

    @Mock
    private GarnetComputeCommandFactory commandFactory;
    @Mock
    private DockerComputeExecutor executor;
    @Mock
    private MpcMapper mpcMapper;
    @Mock
    private MpcTaskMapper taskMapper;
    @Mock
    private My my;
    @Mock
    private GarnetProperties properties;

    private GarnetComputeAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        Path sourceDirectory = temporaryDirectory.resolve("programs");
        Path mountDirectory = temporaryDirectory.resolve("mounted-programs");
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(mountDirectory);
        Files.writeString(sourceDirectory.resolve("demo.mpc"), "program");
        org.mockito.Mockito.lenient().when(my.getBase_path())
                .thenReturn(temporaryDirectory.toString());
        org.mockito.Mockito.lenient().when(properties.getMpcPath())
                .thenReturn(mountDirectory.toString());
        adapter = new GarnetComputeAdapter(
                commandFactory, executor, mpcMapper, taskMapper,
                my, properties);
    }

    @Test
    void compilesAndPersistsReadyTaskWithDerivedMpcName() throws Exception {
        MpcTask task = task();
        Mpc mpc = new Mpc();
        mpc.setPath("programs/demo.mpc");
        DockerComputeCommand command = command("compile");
        ExecutionId executionId = new ExecutionId("EXEC-COMPILE");

        when(mpcMapper.selectById("MPC-1")).thenReturn(mpc);
        when(commandFactory.compile(task, mpc))
                .thenReturn(new GarnetPreparedCommand(command, "demo-128"));
        when(executor.start(command)).thenReturn(executionId);
        when(executor.status(executionId))
                .thenReturn(ExecutionStatus.RUNNING, ExecutionStatus.SUCCEEDED);
        when(executor.collect(executionId)).thenReturn(new ExecutionResult(
                executionId, ExecutionStatus.SUCCEEDED, 0, "compiled", ""));

        adapter.compile(task);

        assertEquals(MpcTask.Status.READY, task.getStatus());
        assertEquals("demo-128", task.getMpcName());
        assertEquals(true, Files.exists(
                temporaryDirectory.resolve("mounted-programs/demo.mpc")));
        InOrder order = inOrder(taskMapper, executor);
        order.verify(taskMapper).updateById(task);
        order.verify(executor).start(command);
        order.verify(executor).collect(executionId);
        order.verify(taskMapper).updateById(task);
    }

    @Test
    void mapsFailedExecutionToFailedTaskAndKeepsStderr() {
        MpcTask task = task();
        Mpc mpc = new Mpc();
        mpc.setPath("programs/demo.mpc");
        DockerComputeCommand command = command("compile");
        ExecutionId executionId = new ExecutionId("EXEC-FAILED");

        when(mpcMapper.selectById("MPC-1")).thenReturn(mpc);
        when(commandFactory.compile(task, mpc))
                .thenReturn(new GarnetPreparedCommand(command, "demo-128"));
        when(executor.start(command)).thenReturn(executionId);
        when(executor.status(executionId)).thenReturn(ExecutionStatus.FAILED);
        when(executor.collect(executionId)).thenReturn(new ExecutionResult(
                executionId, ExecutionStatus.FAILED, 9, "", "compile failed"));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> adapter.compile(task));

        assertEquals(MpcTask.Status.FAILED, task.getStatus());
        assertEquals("compile failed", error.getMessage());
        verify(taskMapper, org.mockito.Mockito.times(2)).updateById(task);
    }

    @Test
    void runsPreparedTaskAndPersistsFinishedState() throws Exception {
        MpcTask task = task();
        task.setMpcName("demo-128");
        DockerComputeCommand command = command("run");
        ExecutionId executionId = new ExecutionId("EXEC-RUN");

        when(commandFactory.run(task)).thenReturn(command);
        when(executor.start(command)).thenReturn(executionId);
        when(executor.status(executionId)).thenReturn(ExecutionStatus.SUCCEEDED);
        when(executor.collect(executionId)).thenReturn(new ExecutionResult(
                executionId, ExecutionStatus.SUCCEEDED, 0, "finished", ""));

        adapter.run(task);

        assertEquals(MpcTask.Status.FINISHED, task.getStatus());
        verify(taskMapper, org.mockito.Mockito.times(2)).updateById(task);
    }

    @Test
    void cancelsCurrentExecutionByTaskId() throws Exception {
        MpcTask task = task();
        task.setMpcName("demo-128");
        DockerComputeCommand command = command("run");
        ExecutionId executionId = new ExecutionId("EXEC-CANCEL");

        when(commandFactory.run(task)).thenReturn(command);
        when(executor.start(command)).thenReturn(executionId);
        CountDownLatch statusEntered = new CountDownLatch(1);
        CountDownLatch cancellationSent = new CountDownLatch(1);
        when(executor.status(executionId)).thenAnswer(invocation -> {
            statusEntered.countDown();
            cancellationSent.await(2, TimeUnit.SECONDS);
            return ExecutionStatus.CANCELLED;
        });
        when(executor.collect(executionId)).thenReturn(new ExecutionResult(
                executionId, ExecutionStatus.CANCELLED, 143, "", ""));

        Thread execution = new Thread(() -> assertThrows(
                IllegalStateException.class,
                () -> adapter.run(task)));
        execution.start();

        assertEquals(true, statusEntered.await(2, TimeUnit.SECONDS));
        adapter.cancel("TASK-1");
        cancellationSent.countDown();
        execution.join(2_000);

        assertEquals(MpcTask.Status.FAILED, task.getStatus());
        verify(executor).cancel(executionId);
    }

    private MpcTask task() {
        MpcTask task = new MpcTask();
        task.setUid("TASK-1");
        task.setMpcId("MPC-1");
        task.setStatus(MpcTask.Status.INIT);
        return task;
    }

    private DockerComputeCommand command(String operation) {
        return new DockerComputeCommand(
                "TASK-1",
                Duration.ofSeconds(5),
                "garnet-container",
                "/usr/src/Garnet",
                List.of(operation));
    }
}
