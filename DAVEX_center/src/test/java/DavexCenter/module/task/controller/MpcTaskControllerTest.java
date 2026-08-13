package DavexCenter.module.task.controller;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.MpcTask;
import DavexBase.task.command.MpcTaskCommand;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.task.service.MpcTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpcTaskControllerTest {

    @TempDir
    Path tempDir;

    @Mock
    private MpcTaskService mpcTaskService;

    @Mock
    private My my;

    @Mock
    private InputMapper inputMapper;

    @InjectMocks
    private MpcTaskController mpcTaskController;

    @Test
    void mapsAgentConnectionFailureToErrorResponse() throws Exception {
        MpcTaskCreateRequest request =
                request(MpcTaskCreateRequest.TaskType.GARNET_MPC);

        when(mpcTaskService.create(any(MpcTaskCommand.class)))
                .thenThrow(new ConnectException("Connection refused"));

        R<MpcTask> response = mpcTaskController.createMpcTask(request);

        assertEquals(0, response.getBody().getCode());
        assertEquals("Connection refused", response.getBody().getMessage());
    }

    @Test
    void mapsContractToCommandAndRunsRegularMpcTask() throws Exception {
        MpcTaskCreateRequest request =
                request(MpcTaskCreateRequest.TaskType.GARNET_MPC);
        MpcTask created = entity("TASK-1", MpcTask.TaskType.GARNET_MPC);
        when(mpcTaskService.create(any(MpcTaskCommand.class)))
                .thenReturn(created);

        R<MpcTask> response = mpcTaskController.createMpcTask(request);

        assertEquals(1, response.getBody().getCode());
        assertEquals("成功创建", response.getBody().getMessage());
        assertSame(created, response.getBody().getData());

        ArgumentCaptor<MpcTaskCommand> commandCaptor =
                ArgumentCaptor.forClass(MpcTaskCommand.class);
        verify(mpcTaskService).create(commandCaptor.capture());
        assertEquals("CENTER-1", commandCaptor.getValue().centerId());
        assertEquals(MpcTaskCommand.TaskType.GARNET_MPC,
                commandCaptor.getValue().taskType());
        verify(mpcTaskService).mpcRun(created);
        verify(mpcTaskService, never()).psiRun(any());
    }

    @Test
    void mapsContractToCommandAndRunsPsiTask() throws Exception {
        MpcTaskCreateRequest request =
                request(MpcTaskCreateRequest.TaskType.GARNET_PSI);
        MpcTask created = entity("TASK-2", MpcTask.TaskType.GARNET_PSI);
        when(mpcTaskService.create(any(MpcTaskCommand.class)))
                .thenReturn(created);

        R<MpcTask> response = mpcTaskController.createMpcTask(request);

        assertEquals(1, response.getBody().getCode());
        assertSame(created, response.getBody().getData());
        verify(mpcTaskService).psiRun(created);
        verify(mpcTaskService, never()).mpcRun(any());
    }

    @Test
    void createsTaskWithUploadedInputAndPassesItThroughCommand() throws Exception {
        MpcTaskCreateRequest request =
                request(MpcTaskCreateRequest.TaskType.GARNET_MPC);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "input.csv",
                "text/csv",
                "id,value\n1,42\n".getBytes(StandardCharsets.UTF_8));
        MpcTask created = entity("TASK-3", MpcTask.TaskType.GARNET_MPC);

        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(my.getIp()).thenReturn("127.0.0.1");
        doAnswer(invocation -> {
            Input input = invocation.getArgument(0);
            input.setUid("INPUT-1");
            return 1;
        }).when(inputMapper).insert(any(Input.class));
        when(mpcTaskService.create(any(MpcTaskCommand.class)))
                .thenReturn(created);

        R<MpcTask> response = mpcTaskController.createWithInput(file, request);

        assertEquals(1, response.getBody().getCode());
        assertSame(created, response.getBody().getData());
        assertEquals("ORIGINAL-INPUT", request.dataId());
        assertEquals("original-host", request.host());
        assertEquals("id,value\n1,42\n",
                Files.readString(tempDir.resolve("Input/input.csv")));

        ArgumentCaptor<Input> inputCaptor = ArgumentCaptor.forClass(Input.class);
        verify(inputMapper).insert(inputCaptor.capture());
        assertEquals("APPLICATION-1", inputCaptor.getValue().getApplicationId());
        assertEquals("Input/input.csv", inputCaptor.getValue().getPath());

        ArgumentCaptor<MpcTaskCommand> commandCaptor =
                ArgumentCaptor.forClass(MpcTaskCommand.class);
        verify(mpcTaskService).create(commandCaptor.capture());
        assertEquals("INPUT-1", commandCaptor.getValue().dataId());
        assertEquals("127.0.0.1", commandCaptor.getValue().host());
        verify(mpcTaskService).mpcRun(created);
    }

    private MpcTaskCreateRequest request(
            MpcTaskCreateRequest.TaskType taskType) {
        return new MpcTaskCreateRequest(
                null,
                "APPLICATION-1",
                "CENTER-1",
                null,
                null,
                null,
                null,
                null,
                "original-host",
                null,
                "ORIGINAL-INPUT",
                taskType,
                null,
                null);
    }

    private MpcTask entity(String uid, MpcTask.TaskType taskType) {
        MpcTask task = new MpcTask();
        task.setUid(uid);
        task.setTaskType(taskType);
        return task;
    }
}
