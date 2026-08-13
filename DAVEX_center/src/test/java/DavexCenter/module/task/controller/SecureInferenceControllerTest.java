package DavexCenter.module.task.controller;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.MpcTask;
import DavexBase.info.InferenceInfo;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.task.service.SecureInferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecureInferenceControllerTest {

    @TempDir
    Path tempDir;

    @Mock
    private SecureInferenceService secureInferenceService;

    @Mock
    private My my;

    @Mock
    private InputMapper inputMapper;

    @InjectMocks
    private SecureInferenceController controller;

    @Test
    void savesInputAndRunsCreatedInferenceEntity() throws Exception {
        InferenceInfo info = inferenceInfo();
        MockMultipartFile file = samplesFile();
        MpcTaskCommand command = inferenceCommand("INPUT-1");
        MpcTask created = new MpcTask();
        created.setUid("TASK-1");
        created.setTaskType(MpcTask.TaskType.GARNET_INFERENCE);

        when(my.getBase_path()).thenReturn(tempDir.toString());
        doAnswer(invocation -> {
            Input input = invocation.getArgument(0);
            input.setUid("INPUT-1");
            return 1;
        }).when(inputMapper).insert(any(Input.class));
        when(secureInferenceService.wrapMpcTaskCommand(info, "INPUT-1"))
                .thenReturn(command);
        when(secureInferenceService.create(command)).thenReturn(created);

        R<MpcTask> response = controller.create(file, info);

        assertEquals(1, response.getBody().getCode());
        assertEquals("成功创建", response.getBody().getMessage());
        assertSame(created, response.getBody().getData());
        assertEquals(
                "feature\n42\n",
                Files.readString(tempDir.resolve("Input/samples.csv")));

        ArgumentCaptor<Input> inputCaptor = ArgumentCaptor.forClass(Input.class);
        verify(inputMapper).insert(inputCaptor.capture());
        assertEquals("APPLICATION-1", inputCaptor.getValue().getApplicationId());
        assertEquals("Input/samples.csv", inputCaptor.getValue().getPath());
        verify(secureInferenceService)
                .wrapMpcTaskCommand(info, "INPUT-1");
        verify(secureInferenceService).create(command);
        verify(secureInferenceService).run(created);
    }

    @Test
    void returnsBusinessErrorWhenInferenceCommandCannotBeWrapped()
            throws Exception {
        InferenceInfo info = inferenceInfo();
        MockMultipartFile file = samplesFile();

        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(secureInferenceService.wrapMpcTaskCommand(info, null))
                .thenThrow(new Exception("MPC not found"));

        R<MpcTask> response = controller.create(file, info);

        assertEquals(0, response.getBody().getCode());
        assertEquals("MPC not found", response.getBody().getMessage());
    }

    private InferenceInfo inferenceInfo() {
        InferenceInfo info = new InferenceInfo();
        info.setAgentId("AGENT-1");
        info.setFileId("MODEL-1");
        info.setApplicationId("APPLICATION-1");
        return info;
    }

    private MockMultipartFile samplesFile() {
        return new MockMultipartFile(
                "file",
                "samples.csv",
                "text/csv",
                "feature\n42\n".getBytes(StandardCharsets.UTF_8));
    }

    private MpcTaskCommand inferenceCommand(String dataId) {
        return new MpcTaskCommand(
                null,
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                null,
                2,
                0L,
                "127.0.0.1",
                6099,
                dataId,
                MpcTaskCommand.TaskType.GARNET_INFERENCE,
                null,
                List.of(new ParticipantInput(
                        "AGENT-1", 1L, "MODEL-1")));
    }
}
