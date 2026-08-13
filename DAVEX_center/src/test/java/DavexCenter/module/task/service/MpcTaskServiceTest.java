package DavexCenter.module.task.service;

import DavexBase.common.My;
import DavexBase.entity.Agent;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.programs.GarnetService;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import DavexCenter.module.task.port.AgentFileMetadataClient;
import DavexCenter.module.task.port.AgentMpcTaskClient;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpcTaskServiceTest {

    @Mock
    private MpcTaskMapper mpcTaskMapper;

    @Mock
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private InputMapper inputMapper;

    @Mock
    private My my;

    @Mock
    private AgentMpcTaskClient agentMpcTaskClient;

    @Mock
    private AgentFileMetadataClient agentFileMetadataClient;

    @Mock
    private GarnetService garnetService;

    @InjectMocks
    private MpcTaskService service;

    @Test
    void assemblesAndSavesEntityBeforeSendingRecipientSpecificContract()
            throws Exception {
        MpcTaskCommand command = command();
        when(my.getId()).thenReturn("CENTER-1");
        when(agentMapper.selectById("AGENT-1")).thenReturn(new Agent());
        doAnswer(invocation -> {
            MpcTask task = invocation.getArgument(0);
            task.setUid("TASK-1");
            return 1;
        }).when(mpcTaskMapper).insert(any(MpcTask.class));
        MpcTask result = service.create(command);

        assertNotSame(command, result);
        assertEquals("TASK-1", result.getUid());
        assertEquals("APPLICATION-1", result.getApplicationId());
        assertEquals("CENTER-1", result.getCenterId());
        assertEquals("MPC-1", result.getMpcId());
        assertEquals(MpcTask.TaskType.GARNET_MPC, result.getTaskType());
        assertEquals(128, result.getCompileParameters().get("prime"));
        verify(mpcTaskMapper).insert(result);

        ArgumentCaptor<MpcTaskAgent> participantCaptor =
                ArgumentCaptor.forClass(MpcTaskAgent.class);
        verify(mpcTaskAgentMapper).insert(participantCaptor.capture());
        MpcTaskAgent participant = participantCaptor.getValue();
        assertEquals("TASK-1", participant.getMpcTaskId());
        assertEquals("CENTER-1", participant.getCenterId());
        assertEquals("AGENT-1", participant.getAgentId());
        assertEquals(1L, participant.getPart());

        ArgumentCaptor<MpcTaskCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(MpcTaskCreateRequest.class);
        verify(agentMpcTaskClient).createTask(
                org.mockito.ArgumentMatchers.eq("AGENT-1"),
                requestCaptor.capture());
        MpcTaskCreateRequest request = requestCaptor.getValue();
        assertEquals("TASK-1", request.uid());
        assertEquals("CENTER-1", request.centerId());
        assertEquals("MPC-1", request.mpcId());
        assertEquals("FILE-1", request.dataId());
        assertEquals(1L, request.part());
        assertEquals(MpcTaskCreateRequest.TaskType.GARNET_MPC,
                request.taskType());
        assertEquals(1, request.partInfo().size());
        assertNull(request.partInfo().get(0).fileID());
    }

    @Test
    void updatesPsiCompileParametersWithoutMutatingOriginalCommand()
            throws Exception {
        MpcTaskCommand command = new MpcTaskCommand(
                null,
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(Map.of("old", 1), Map.of("batch", 32)),
                2,
                0L,
                "127.0.0.1",
                9000,
                "CENTER-FILE",
                MpcTaskCommand.TaskType.GARNET_PSI,
                MpcTaskCommand.Status.INIT,
                List.of(new ParticipantInput("AGENT-1", 1L, "FILE-1")));
        Input input = new Input();
        input.setPath("Input/center.csv");
        when(inputMapper.selectById("CENTER-FILE")).thenReturn(input);
        when(garnetService.csvCount("Input/center.csv")).thenReturn(8L);
        when(agentFileMetadataClient.getRowCount("AGENT-1", "FILE-1"))
                .thenReturn(11L);

        MpcTaskCommand updated = service.parameterUpdate(command);

        assertNotSame(command, updated);
        assertEquals(8L, updated.options().compileParameters().get("P0_Data"));
        assertEquals(10L, updated.options().compileParameters().get("P1_Data"));
        assertEquals(32, updated.options().runtimeParameters().get("batch"));
        assertEquals(1, command.options().compileParameters().get("old"));
        assertSame(command.participants().get(0), updated.participants().get(0));
    }

    private MpcTaskCommand command() {
        Map<String, Object> compileParameters = new LinkedHashMap<>();
        compileParameters.put("prime", 128);
        Map<String, Object> runtimeParameters = new LinkedHashMap<>();
        runtimeParameters.put("batch", 32);

        return new MpcTaskCommand(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(compileParameters, runtimeParameters),
                2,
                0L,
                "127.0.0.1",
                9000,
                "CENTER-FILE",
                MpcTaskCommand.TaskType.GARNET_MPC,
                MpcTaskCommand.Status.INIT,
                List.of(new ParticipantInput("AGENT-1", 1L, "FILE-1")));
    }
}
