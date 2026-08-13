package DavexCenter.module.envelope;

import DavexBase.common.envelope.Sharing;
import DavexBase.info.InferenceInfo;
import DavexCenter.module.task.controller.MpcTaskController;
import DavexCenter.module.task.controller.SecureInferenceController;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnvelopeServiceTest {

    @Mock
    private MpcTaskController mpcTaskController;

    @Mock
    private SecureInferenceController secureInferenceController;

    @InjectMocks
    private EnvelopeService envelopeService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @EnumSource(value = Sharing.SharingType.class, names = {"MPC", "PSI"})
    void dispatchesMpcAndPsiSettingsToMpcController(Sharing.SharingType sharingType) throws Exception {
        MpcTaskCreateRequest.TaskType taskType =
                sharingType == Sharing.SharingType.PSI
                        ? MpcTaskCreateRequest.TaskType.GARNET_PSI
                        : MpcTaskCreateRequest.TaskType.GARNET_MPC;
        Sharing sharing = sharing(
                sharingType,
                JSONObject.parseObject("""
                        {
                          "uid": "TASK-1",
                          "applicationId": "APPLICATION-1",
                          "centerId": "CENTER-1",
                          "mpcId": "MPC-1",
                          "taskType": "%s",
                          "partInfo": [
                            {
                              "agentID": "AGENT-1",
                              "part": 1,
                              "fileID": "FILE-1"
                            }
                          ]
                        }
                        """.formatted(taskType.name()))
        );
        MockMultipartFile data = new MockMultipartFile(
                "file",
                "input.csv",
                "text/csv",
                "id,value\n1,42\n".getBytes()
        );

        envelopeService.sharingDispatch(sharing, data);

        ArgumentCaptor<MpcTaskCreateRequest> taskCaptor =
                ArgumentCaptor.forClass(MpcTaskCreateRequest.class);
        verify(mpcTaskController).createWithInput(same(data), taskCaptor.capture());
        MpcTaskCreateRequest task = taskCaptor.getValue();
        assertEquals("TASK-1", task.uid());
        assertEquals("APPLICATION-1", task.applicationId());
        assertEquals("CENTER-1", task.centerId());
        assertEquals("MPC-1", task.mpcId());
        assertEquals(taskType, task.taskType());
        assertNotNull(task.partInfo());
        assertEquals(1, task.partInfo().size());
        assertEquals("AGENT-1", task.partInfo().get(0).agentID());
        assertEquals(1L, task.partInfo().get(0).part());
        assertEquals("FILE-1", task.partInfo().get(0).fileID());
    }

    @Test
    void dispatchesInferenceSettingsToSecureInferenceController() throws Exception {
        Sharing sharing = sharing(
                Sharing.SharingType.SECURITY_INFERENCE,
                JSONObject.parseObject("""
                        {
                          "agentId": "AGENT-1",
                          "fileId": "MODEL-1",
                          "applicationId": "APPLICATION-1"
                        }
                        """)
        );
        MockMultipartFile data = new MockMultipartFile(
                "file",
                "samples.csv",
                "text/csv",
                "feature\n42\n".getBytes()
        );

        envelopeService.sharingDispatch(sharing, data);

        ArgumentCaptor<InferenceInfo> infoCaptor = ArgumentCaptor.forClass(InferenceInfo.class);
        verify(secureInferenceController).create(same(data), infoCaptor.capture());
        InferenceInfo info = infoCaptor.getValue();
        assertEquals("AGENT-1", info.getAgentId());
        assertEquals("MODEL-1", info.getFileId());
        assertEquals("APPLICATION-1", info.getApplicationId());
    }

    private Sharing sharing(Sharing.SharingType type, JSONObject setting) {
        Sharing sharing = new Sharing();
        sharing.setType(type);
        sharing.setSetting(setting);
        return sharing;
    }
}
