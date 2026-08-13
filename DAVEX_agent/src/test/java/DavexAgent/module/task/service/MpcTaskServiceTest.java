package DavexAgent.module.task.service;

import DavexBase.common.My;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    private MpcMapper mpcMapper;

    @Mock
    private MpcService mpcService;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private FileFolderService fileFolderService;

    @InjectMocks
    private MpcTaskService service;

    @Test
    void savesCommandAsEntityAndPreprocessesRegularMpcTask()
            throws Exception {

        MpcTaskCommand command = command(
                MpcTaskCommand.TaskType.GARNET_MPC,
                Map.of());
        File input = inputFile();
        when(mpcMapper.selectById("MPC-1")).thenReturn(new Mpc());
        when(fileMapper.selectById("FILE-1")).thenReturn(input);
        when(my.getBase_path()).thenReturn("/data/davex");
        when(fileFolderService.getFilePath(input, "/data/davex"))
                .thenReturn("/data/davex/input.csv");

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
        verify(garnetService).compile(savedTask);
        verify(garnetService).link(
                "/data/davex/input.csv", "TASK-1", 1L);
        verify(garnetService, never()).csvExtract(
                any(), any(), any(), any());
        verify(mpcService, never()).downloadMPC(any(), any());
    }

    @Test
    void savesCommandAsEntityAndPreprocessesPsiTask()
            throws Exception {

        MpcTaskCommand command = command(
                MpcTaskCommand.TaskType.GARNET_PSI,
                Map.of("PK", "id"));
        File input = inputFile();
        when(mpcMapper.selectById("MPC-1")).thenReturn(new Mpc());
        when(fileMapper.selectById("FILE-1")).thenReturn(input);
        when(my.getBase_path()).thenReturn("/data/davex");
        when(fileFolderService.getFilePath(input, "/data/davex"))
                .thenReturn("/data/davex/input.csv");

        service.createMpcTask(command);

        assertParticipantWasSaved();
        MpcTask savedTask = captureSavedTask();
        assertEquals(MpcTask.TaskType.GARNET_PSI,
                savedTask.getTaskType());
        assertEquals("id",
                savedTask.getRuntimeParameters().getString("PK"));
        verify(garnetService).compile(savedTask);
        verify(garnetService).csvExtract(
                "/data/davex/input.csv", "id", "TASK-1", 1L);
        verify(garnetService, never()).link(any(), any(), any());
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
        verify(garnetService, never()).compile(any());
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
}
