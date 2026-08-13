package DavexAgent.module.task.service;

import DavexBase.entity.File;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
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
class SecureInferenceServiceTest {

    @Mock
    private FileMapper fileMapper;

    @Mock
    private MpcMapper mpcMapper;

    @Mock
    private MpcTaskService mpcTaskService;

    @Mock
    private MpcTaskMapper mpcTaskMapper;

    @Mock
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @InjectMocks
    private SecureInferenceService service;

    @Test
    void savesValidInferenceCommandAndDelegatesEntityPreprocessing()
            throws Exception {

        MpcTaskCommand command = inferenceCommand(
                MpcTaskCommand.TaskType.GARNET_INFERENCE);
        File model = modelFile("secureInferModel");
        when(fileMapper.selectById("MODEL-1")).thenReturn(model);

        service.create(command);

        ArgumentCaptor<MpcTaskAgent> participantCaptor =
                ArgumentCaptor.forClass(MpcTaskAgent.class);
        verify(mpcTaskAgentMapper).insert(participantCaptor.capture());
        MpcTaskAgent participant = participantCaptor.getValue();
        assertEquals("TASK-1", participant.getMpcTaskId());
        assertEquals("CENTER-1", participant.getCenterId());
        assertEquals("AGENT-1", participant.getAgentId());
        assertEquals(1L, participant.getPart());

        ArgumentCaptor<MpcTask> taskCaptor =
                ArgumentCaptor.forClass(MpcTask.class);
        verify(mpcTaskMapper).insert(taskCaptor.capture());
        MpcTask entity = taskCaptor.getValue();
        assertEquals("TASK-1", entity.getUid());
        assertEquals("CENTER-1", entity.getCenterId());
        assertEquals("MODEL-1", entity.getDataId());
        assertEquals(MpcTask.TaskType.GARNET_INFERENCE,
                entity.getTaskType());
        verify(mpcTaskService).preprocess(entity);
    }

    @Test
    void rejectsDuplicateInferenceCommand() throws Exception {
        MpcTaskCommand command = inferenceCommand(
                MpcTaskCommand.TaskType.GARNET_INFERENCE);
        when(mpcTaskMapper.selectById("TASK-1"))
                .thenReturn(new MpcTask());

        Exception error = assertThrows(
                Exception.class,
                () -> service.create(command));

        assertEquals("任务已存在", error.getMessage());
        verify(mpcTaskAgentMapper, never()).insert(any());
        verify(mpcTaskService, never()).preprocess(any());
    }

    @Test
    void rejectsWrongInferenceCommandType() {
        MpcTaskCommand command = inferenceCommand(
                MpcTaskCommand.TaskType.GARNET_MPC);

        Exception error = assertThrows(
                Exception.class,
                () -> service.create(command));

        assertEquals("任务类型不匹配", error.getMessage());
        verify(fileMapper, never()).selectById(any());
    }

    @Test
    void rejectsMissingModelFile() {
        MpcTaskCommand command = inferenceCommand(
                MpcTaskCommand.TaskType.GARNET_INFERENCE);

        Exception error = assertThrows(
                Exception.class,
                () -> service.create(command));

        assertEquals("文件不存在", error.getMessage());
        verify(mpcTaskMapper, never()).insert(any());
    }

    @Test
    void rejectsWrongModelFileType() {
        MpcTaskCommand command = inferenceCommand(
                MpcTaskCommand.TaskType.GARNET_INFERENCE);
        when(fileMapper.selectById("MODEL-1"))
                .thenReturn(modelFile("csv"));

        Exception error = assertThrows(
                Exception.class,
                () -> service.create(command));

        assertEquals("文件类型不符", error.getMessage());
        verify(mpcTaskMapper, never()).insert(any());
    }

    private MpcTaskCommand inferenceCommand(
            MpcTaskCommand.TaskType taskType) {

        return new MpcTaskCommand(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(Map.of(), Map.of()),
                2,
                1L,
                "127.0.0.1",
                6099,
                "MODEL-1",
                taskType,
                MpcTaskCommand.Status.INIT,
                List.of(new ParticipantInput(
                        "AGENT-1", 1L, "MODEL-1")));
    }

    private File modelFile(String type) {
        File file = new File();
        file.setUid("MODEL-1");
        file.setType(type);
        return file;
    }
}
