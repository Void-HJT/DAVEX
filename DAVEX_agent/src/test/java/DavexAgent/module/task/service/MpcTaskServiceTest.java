package DavexAgent.module.task.service;

import DavexAgent.module.task.port.CenterMpcResultClient;
import DavexAgent.module.task.port.CenterTaskNotificationClient;
import com.alibaba.fastjson.JSONObject;
import DavexBase.common.My;
import DavexBase.compute.garnet.GarnetComputeAdapter;
import DavexBase.entity.File;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.directory.FileFolderService;
import DavexBase.service.programs.GarnetService;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpcTaskServiceTest {

    @Mock
    private MpcTaskMapper mpcTaskMapper;

    @Mock
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Mock
    private My my;

    @Mock
    private GarnetService garnetService;

    @Mock
    private GarnetComputeAdapter garnetComputeAdapter;

    @Mock
    private MpcMapper mpcMapper;

    @Mock
    private MpcService mpcService;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private FileFolderService fileFolderService;

    @Mock
    private CenterTaskNotificationClient notificationClient;

    @Mock
    private CenterMpcResultClient resultClient;

    @InjectMocks
    private MpcTaskService service;

    @Test
    void savesCommandAsEntityAndPreprocessesRegularMpcTask(
            @TempDir Path tempDir)
            throws Exception {

        MpcTaskCommand command = command(
                MpcTaskCommand.TaskType.GARNET_MPC,
                Map.of());
        File input = inputFile();
        when(mpcMapper.selectById("MPC-1"))
                .thenReturn(existingProgram(tempDir));
        when(fileMapper.selectById("FILE-1")).thenReturn(input);
        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(fileFolderService.getFilePath(input, tempDir.toString()))
                .thenReturn(tempDir.resolve("input.csv").toString());

        service.createMpcTask(command);

        assertParticipantWasSaved();
        MpcTask savedTask = captureSavedTask();
        assertEquals("TASK-1", savedTask.getUid());
        assertEquals("CENTER-1", savedTask.getCenterId());
        assertEquals("FILE-1", savedTask.getDataId());
        assertEquals(128,
                savedTask.getCompileParameters().getInteger("prime"));
        assertEquals(MpcTask.TaskType.GARNET_MPC,
                savedTask.getTaskType());
        verify(garnetComputeAdapter).compile(savedTask);
        verify(garnetService).link(
                tempDir.resolve("input.csv").toString(), "TASK-1", 1L);
        verify(garnetService, never()).csvExtract(
                any(), any(), any(), any());
        verify(mpcService, never()).downloadMPC(any(), any());
    }

    @Test
    void savesCommandAsEntityAndPreprocessesPsiTask(
            @TempDir Path tempDir)
            throws Exception {

        MpcTaskCommand command = command(
                MpcTaskCommand.TaskType.GARNET_PSI,
                Map.of("PK", "id"));
        File input = inputFile();
        when(mpcMapper.selectById("MPC-1"))
                .thenReturn(existingProgram(tempDir));
        when(fileMapper.selectById("FILE-1")).thenReturn(input);
        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(fileFolderService.getFilePath(input, tempDir.toString()))
                .thenReturn(tempDir.resolve("input.csv").toString());

        service.createMpcTask(command);

        assertParticipantWasSaved();
        MpcTask savedTask = captureSavedTask();
        assertEquals(MpcTask.TaskType.GARNET_PSI,
                savedTask.getTaskType());
        assertEquals("id",
                savedTask.getRuntimeParameters().getString("PK"));
        verify(garnetComputeAdapter).compile(savedTask);
        verify(garnetService).csvExtract(
                tempDir.resolve("input.csv").toString(),
                "id", "TASK-1", 1L);
        verify(garnetService, never()).link(any(), any(), any());
    }

    @Test
    void redownloadsMpcWhenMetadataExistsButProgramFileIsMissing(
            @TempDir Path tempDir) throws Exception {

        Mpc localMpc = new Mpc();
        localMpc.setUid("MPC-1");
        localMpc.setPath(Path.of("programs", "missing.mpc").toString());
        when(mpcMapper.selectById("MPC-1")).thenReturn(localMpc);
        when(my.getBase_path()).thenReturn(tempDir.toString());

        File input = inputFile();
        when(fileMapper.selectById("FILE-1")).thenReturn(input);
        when(fileFolderService.getFilePath(input, tempDir.toString()))
                .thenReturn(tempDir.resolve("input.csv").toString());

        service.createMpcTask(command(
                MpcTaskCommand.TaskType.GARNET_MPC,
                Map.of()));

        verify(mpcService).downloadMPC("CENTER-1", "MPC-1");
        verify(garnetComputeAdapter).compile(any(MpcTask.class));
    }

    @Test
    void rejectsDuplicateCommandBeforeCreatingPersistenceRecords()
            throws Exception {

        MpcTaskCommand command = command(
                MpcTaskCommand.TaskType.GARNET_MPC,
                Map.of());
        when(mpcTaskMapper.selectById("TASK-1"))
                .thenReturn(new MpcTask());

        Exception error = assertThrows(
                Exception.class,
                () -> service.createMpcTask(command));

        assertEquals("任务已存在", error.getMessage());
        verify(mpcTaskAgentMapper, never()).insert(any());
        verify(mpcTaskMapper, never()).insert(any());
        verify(garnetComputeAdapter, never()).compile(any());
    }

    @Test
    void reportsMpcCompileFailureThroughNotificationPort()
            throws Exception {
        MpcTask task = task(MpcTask.TaskType.GARNET_MPC);
        doThrow(new IllegalStateException("compile failed"))
                .when(garnetComputeAdapter).compile(task);

        Exception error = assertThrows(
                Exception.class,
                () -> service.preprocess(task));

        assertEquals("compile failed", error.getMessage());
        verify(notificationClient).sendNotification(
                "CENTER-1",
                "APPLICATION-1",
                "MPC任务编译失败",
                "MPC任务编译失败\n任务ID: TASK-1\n任务类型: GARNET_MPC\n错误信息: compile failed",
                "TASK-1",
                0,
                "mpc");
    }

    @Test
    void uploadsPsiResultAndReportsSuccess(@TempDir Path tempDir)
            throws Exception {
        MpcTask task = task(MpcTask.TaskType.GARNET_PSI);
        JSONObject runtimeParameters = new JSONObject();
        runtimeParameters.put("PK", "id");
        task.setRuntimeParameters(runtimeParameters);
        task.setStatus(MpcTask.Status.FINISHED);

        File input = inputFile();
        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(fileMapper.selectById("FILE-1")).thenReturn(input);
        when(fileFolderService.getFilePath(input, tempDir.toString()))
                .thenReturn(tempDir.resolve("input.csv").toString());
        doAnswer(invocation -> {
            Path output = invocation.getArgument(4);
            Files.writeString(output, "id\n1001\n");
            return null;
        }).when(garnetService).csvQuery(
                any(), any(), any(), any(), any(Path.class));

        service.psiRun(task);

        verify(garnetComputeAdapter).run(task);
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<DavexBase.entity.MpcTaskOutput> metadataCaptor =
                ArgumentCaptor.forClass(DavexBase.entity.MpcTaskOutput.class);
        verify(resultClient).uploadPsiResult(
                org.mockito.ArgumentMatchers.eq("CENTER-1"),
                pathCaptor.capture(),
                metadataCaptor.capture());
        assertEquals("TASK-1.csv", pathCaptor.getValue().getFileName().toString());
        assertEquals("TASK-1", metadataCaptor.getValue().getTaskId());
        assertEquals("APPLICATION-1", metadataCaptor.getValue().getApplicationId());
        assertEquals("TASK-1.csv", metadataCaptor.getValue().getName());
        verify(notificationClient).sendNotification(
                "CENTER-1",
                "APPLICATION-1",
                "PSI任务运行结束",
                "PSI任务结果保存成功\n任务ID: TASK-1\n任务类型: GARNET_PSI\n运行结果: FINISHED",
                "TASK-1",
                1,
                "psi");
    }

    private void assertParticipantWasSaved() {
        ArgumentCaptor<MpcTaskAgent> participantCaptor =
                ArgumentCaptor.forClass(MpcTaskAgent.class);
        verify(mpcTaskAgentMapper).insert(participantCaptor.capture());
        MpcTaskAgent participant = participantCaptor.getValue();
        assertEquals("TASK-1", participant.getMpcTaskId());
        assertEquals("CENTER-1", participant.getCenterId());
        assertEquals("AGENT-1", participant.getAgentId());
        assertEquals(1L, participant.getPart());
    }

    private MpcTask captureSavedTask() {
        ArgumentCaptor<MpcTask> taskCaptor =
                ArgumentCaptor.forClass(MpcTask.class);
        verify(mpcTaskMapper).insert(taskCaptor.capture());
        return taskCaptor.getValue();
    }

    private MpcTaskCommand command(
            MpcTaskCommand.TaskType taskType,
            Map<String, Object> runtimeParameters) {

        return new MpcTaskCommand(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(
                        Map.of("prime", 128),
                        runtimeParameters),
                2,
                1L,
                "127.0.0.1",
                6000,
                "FILE-1",
                taskType,
                MpcTaskCommand.Status.INIT,
                List.of(new ParticipantInput(
                        "AGENT-1", 1L, "FILE-1")));
    }

    private File inputFile() {
        File file = new File();
        file.setUid("FILE-1");
        file.setName("input.csv");
        return file;
    }

    private Mpc existingProgram(Path tempDir) throws Exception {
        Path program = tempDir.resolve("programs/demo.mpc");
        Files.createDirectories(program.getParent());
        Files.writeString(program, "program");

        Mpc mpc = new Mpc();
        mpc.setUid("MPC-1");
        mpc.setPath(Path.of("programs", "demo.mpc").toString());
        return mpc;
    }

    private MpcTask task(MpcTask.TaskType taskType) {
        MpcTask task = new MpcTask();
        task.setUid("TASK-1");
        task.setApplicationId("APPLICATION-1");
        task.setCenterId("CENTER-1");
        task.setDataId("FILE-1");
        task.setPart(1L);
        task.setTaskType(taskType);
        return task;
    }
}
