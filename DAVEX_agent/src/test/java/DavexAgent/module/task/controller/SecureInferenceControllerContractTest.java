package DavexAgent.module.task.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;

import DavexAgent.module.task.service.SecureInferenceService;
import DavexBase.common.R;
import DavexBase.task.command.MpcTaskCommand;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 验证安全推理入口将通信协议隔离在 Controller 边界。
 */
@ExtendWith(MockitoExtension.class)
class SecureInferenceControllerContractTest {

    @Mock
    private SecureInferenceService secureInferenceService;

    @InjectMocks
    private SecureInferenceController controller;

    @Test
    void convertsContractToCommandBeforeCallingService() throws Exception {
        R<?> response = controller.create(createRequest());

        ArgumentCaptor<MpcTaskCommand> captor =
                ArgumentCaptor.forClass(MpcTaskCommand.class);
        verify(secureInferenceService).create(captor.capture());

        MpcTaskCommand command = captor.getValue();
        assertEquals("TASK-1", command.uid());
        assertEquals("CENTER-1", command.centerId());
        assertEquals("FILE-1", command.dataId());
        assertEquals(
                MpcTaskCommand.TaskType.GARNET_INFERENCE,
                command.taskType());
        assertEquals("AGENT-1",
                command.participants().get(0).agentId());
        assertEquals(1, response.getBody().getCode());
        assertEquals("成功创建", response.getBody().getMessage());
    }

    @Test
    void preservesServiceErrorResponse() throws Exception {
        doThrow(new Exception("文件不存在"))
                .when(secureInferenceService)
                .create(any(MpcTaskCommand.class));

        R<?> response = controller.create(createRequest());

        assertEquals(0, response.getBody().getCode());
        assertEquals("文件不存在", response.getBody().getMessage());
    }

    private MpcTaskCreateRequest createRequest() {
        return new MpcTaskCreateRequest(
                "TASK-1",
                "APP-1",
                "CENTER-1",
                "MPC-1",
                null,
                null,
                2,
                1L,
                "127.0.0.1",
                6099,
                "FILE-1",
                MpcTaskCreateRequest.TaskType.GARNET_INFERENCE,
                MpcTaskCreateRequest.Status.INIT,
                List.of(new MpcTaskCreateRequest.PartInfo(
                        "AGENT-1",
                        1L,
                        "FILE-1"
                ))
        );
    }
}
