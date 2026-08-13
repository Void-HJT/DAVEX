package DavexAgent.module.task.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;

import DavexAgent.module.task.service.MpcTaskService;
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
 * 验证 Agent Controller 将通信协议隔离在接口边界。
 */
@ExtendWith(MockitoExtension.class)
class MpcTaskControllerContractTest {

    @Mock
    private MpcTaskService mpcTaskService;

    @InjectMocks
    private MpcTaskController controller;

    @Test
    void convertsContractToCommandBeforeCallingService() throws Exception {
        R<?> response = controller.createMpcTask(createRequest());

        ArgumentCaptor<MpcTaskCommand> captor =
                ArgumentCaptor.forClass(MpcTaskCommand.class);
        verify(mpcTaskService).createMpcTask(captor.capture());

        MpcTaskCommand command = captor.getValue();
        assertEquals("TASK-1", command.uid());
        assertEquals("CENTER-1", command.centerId());
        assertEquals("AGENT-1", command.participants().get(0).agentId());
        assertEquals("FILE-1", command.participants().get(0).fileId());
        assertEquals(MpcTaskCommand.TaskType.GARNET_MPC, command.taskType());
        assertEquals(1, response.getBody().getCode());
        assertEquals("成功创建", response.getBody().getMessage());
    }

    @Test
    void preservesServiceErrorResponse() throws Exception {
        doThrow(new Exception("任务已存在"))
                .when(mpcTaskService)
                .createMpcTask(any(MpcTaskCommand.class));

        R<?> response = controller.createMpcTask(createRequest());

        assertEquals(0, response.getBody().getCode());
        assertEquals("任务已存在", response.getBody().getMessage());
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
                6000,
                "FILE-0",
                MpcTaskCreateRequest.TaskType.GARNET_MPC,
                MpcTaskCreateRequest.Status.INIT,
                List.of(new MpcTaskCreateRequest.PartInfo(
                        "AGENT-1",
                        1L,
                        "FILE-1"
                ))
        );
    }
}
