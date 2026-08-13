package DavexCenter.module.task.service;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.Agent;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.auth.CenterWebClientService;
import DavexBase.service.programs.GarnetService;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import DavexCenter.entity.Input;
import DavexCenter.mapper.InputMapper;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
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
    private CenterWebClientService centerWebClientService;

    @Mock
    private GarnetService garnetService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

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
        stubAgentPost("/MpcTasks/create");

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

        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(requestSpec).bodyValue(bodyCaptor.capture());
        MpcTaskCreateRequest request =
                (MpcTaskCreateRequest) bodyCaptor.getValue();
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
    @SuppressWarnings({"rawtypes", "unchecked"})
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
        when(centerWebClientService.center2AgentWebClient("AGENT-1"))
                .thenReturn(webClient);
        when(webClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(any(java.util.function.Function.class)))
                .thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(Body.success(11L, "ok")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));

        MpcTaskCommand updated = service.parameterUpdate(command);

        assertNotSame(command, updated);
        assertEquals(8L, updated.options().compileParameters().get("P0_Data"));
        assertEquals(10L, updated.options().compileParameters().get("P1_Data"));
        assertEquals(32, updated.options().runtimeParameters().get("batch"));
        assertEquals(1, command.options().compileParameters().get("old"));
        assertSame(command.participants().get(0), updated.participants().get(0));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubAgentPost(String uri) throws Exception {
        when(centerWebClientService.center2AgentWebClient("AGENT-1"))
                .thenReturn(webClient);
        when(webClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(uri)).thenReturn(requestSpec);
        doReturn(headersSpec).when(requestSpec).bodyValue(any());
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(R.success("created")))
                .when(responseSpec)
                .bodyToMono(any(ParameterizedTypeReference.class));
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
