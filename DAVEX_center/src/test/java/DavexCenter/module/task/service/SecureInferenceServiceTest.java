package DavexCenter.module.task.service;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.Agent;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.info.InferenceInfo;
import DavexBase.info.Parameter;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.service.auth.CenterWebClientService;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecureInferenceServiceTest {

    @Mock
    private MpcMapper mpcMapper;

    @Mock
    private MpcTaskMapper mpcTaskMapper;

    @Mock
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private My my;

    @Mock
    private CenterWebClientService centerWebClientService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Spy
    @InjectMocks
    private SecureInferenceService service;

    @Test
    void savesEntityAndSendsGeneratedUidInMaskedAgentContract()
            throws Exception {
        MpcTaskCommand command = inferenceCommand();
        when(my.getId()).thenReturn("CENTER-1");
        when(agentMapper.selectById("AGENT-1")).thenReturn(new Agent());
        doAnswer(invocation -> {
            MpcTask task = invocation.getArgument(0);
            task.setUid("TASK-1");
            return 1;
        }).when(mpcTaskMapper).insert(any(MpcTask.class));
        stubAgentPost("/SecureInference/create");

        MpcTask result = service.create(command);

        assertNotSame(command, result);
        assertEquals("TASK-1", result.getUid());
        assertEquals(MpcTask.TaskType.GARNET_INFERENCE,
                result.getTaskType());
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
        assertEquals("MODEL-1", request.dataId());
        assertEquals(1L, request.part());
        assertEquals(MpcTaskCreateRequest.TaskType.GARNET_INFERENCE,
                request.taskType());
        assertNull(request.partInfo().get(0).fileID());
    }

    @Test
    void wrapsInferenceInfoAsCommandUsingDownloadedMpcDefaults()
            throws Exception {
        InferenceInfo info = new InferenceInfo();
        info.setAgentId("AGENT-1");
        info.setFileId("MODEL-1");
        info.setApplicationId("APPLICATION-1");
        Mpc mpc = mpcWithDefaults();

        when(my.getIp()).thenReturn("127.0.0.1");
        when(my.getId()).thenReturn("CENTER-1");
        doReturn(mpc).when(service).downloadMPC("AGENT-1", "MODEL-1");
        when(mpcMapper.selectById("MPC-1")).thenReturn(mpc);

        MpcTaskCommand command =
                service.wrapMpcTaskCommand(info, "INPUT-1");

        assertEquals("127.0.0.1", command.host());
        assertEquals(6099, command.port());
        assertEquals(2, command.n());
        assertEquals("APPLICATION-1", command.applicationId());
        assertEquals("CENTER-1", command.centerId());
        assertEquals("MPC-1", command.mpcId());
        assertEquals(0L, command.part());
        assertEquals("INPUT-1", command.dataId());
        assertEquals(MpcTaskCommand.TaskType.GARNET_INFERENCE,
                command.taskType());
        assertEquals("32",
                command.options().compileParameters().get("bitLength"));
        assertEquals("semi2k-party",
                command.options().runtimeParameters().get("protocol"));
        assertEquals(1, command.participants().size());
        assertEquals("AGENT-1", command.participants().get(0).agentId());
        assertEquals(1L, command.participants().get(0).part());
        assertEquals("MODEL-1", command.participants().get(0).fileId());
    }

    @Test
    void wrapsInferenceInfoUsingRequestedParametersInsteadOfDefaults()
            throws Exception {
        InferenceInfo info = new InferenceInfo();
        info.setAgentId("AGENT-1");
        info.setFileId("MODEL-1");
        info.setApplicationId("APPLICATION-1");
        info.setCompileParameters(Map.of(
                "m", 2,
                "test_samples", 4,
                "label_number", 2,
                "tree_h", 3,
                "Ring", "64"));
        info.setRuntimeParameters(Map.of(
                "protocol", "semi2k-party"));
        Mpc mpc = mpcWithDefaults();

        when(my.getIp()).thenReturn("127.0.0.1");
        when(my.getId()).thenReturn("CENTER-1");
        doReturn(mpc).when(service).downloadMPC("AGENT-1", "MODEL-1");
        when(mpcMapper.selectById("MPC-1")).thenReturn(mpc);

        MpcTaskCommand command =
                service.wrapMpcTaskCommand(info, "INPUT-1");

        assertEquals(2, command.options().compileParameters().get("m"));
        assertEquals(4,
                command.options().compileParameters().get("test_samples"));
        assertEquals(2,
                command.options().compileParameters().get("label_number"));
        assertEquals(3,
                command.options().compileParameters().get("tree_h"));
        assertEquals("64",
                command.options().compileParameters().get("Ring"));
        assertEquals("semi2k-party",
                command.options().runtimeParameters().get("protocol"));
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

    private MpcTaskCommand inferenceCommand() {
        return new MpcTaskCommand(
                null,
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(Map.of("bitLength", "32"),
                        Map.of("protocol", "semi2k-party")),
                2,
                0L,
                "127.0.0.1",
                6099,
                "INPUT-1",
                MpcTaskCommand.TaskType.GARNET_INFERENCE,
                null,
                List.of(new ParticipantInput(
                        "AGENT-1", 1L, "MODEL-1")));
    }

    private Mpc mpcWithDefaults() throws Exception {
        Mpc mpc = new Mpc();
        mpc.setUid("MPC-1");
        mpc.setCompileParameters(List.of(
                stringParameter("bitLength", "32")));
        mpc.setRuntimeParameters(List.of(
                stringParameter("protocol", "semi2k-party")));
        return mpc;
    }

    private Parameter stringParameter(String name, String defaultValue) {
        return new Parameter(
                name,
                Parameter.ArgumentsType.HYPER,
                Parameter.LimitType.STRING,
                null,
                new Parameter.STRINGLimit(defaultValue),
                "",
                false);
    }
}
