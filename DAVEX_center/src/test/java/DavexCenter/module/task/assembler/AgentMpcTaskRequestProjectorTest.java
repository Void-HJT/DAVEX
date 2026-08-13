package DavexCenter.module.task.assembler;

import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AgentMpcTaskRequestProjectorTest {

    @Test
    void selectsRecipientInputAndMasksEveryParticipantFileId() {
        ParticipantInput first =
                new ParticipantInput("AGENT-1", 1L, "FILE-1");
        ParticipantInput recipient =
                new ParticipantInput("AGENT-2", 2L, "FILE-2");
        MpcTaskCommand command = new MpcTaskCommand(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(Map.of("prime", 128), Map.of("batch", 32)),
                3,
                0L,
                "127.0.0.1",
                9000,
                "CENTER-FILE",
                MpcTaskCommand.TaskType.GARNET_MPC,
                MpcTaskCommand.Status.INIT,
                List.of(first, recipient));

        MpcTaskCreateRequest request =
                AgentMpcTaskRequestProjector.toRequest(command, recipient);

        assertEquals("TASK-1", request.uid());
        assertEquals("FILE-2", request.dataId());
        assertEquals(2L, request.part());
        assertEquals(128, request.compileParameters().get("prime"));
        assertEquals(32, request.runtimeParameters().get("batch"));
        assertEquals(MpcTaskCreateRequest.TaskType.GARNET_MPC,
                request.taskType());
        assertEquals(MpcTaskCreateRequest.Status.INIT, request.status());
        assertEquals(2, request.partInfo().size());
        assertNull(request.partInfo().get(0).fileID());
        assertNull(request.partInfo().get(1).fileID());
    }

    @Test
    void preservesNullCommandAndParticipantListSemantics() {
        ParticipantInput recipient =
                new ParticipantInput("AGENT-1", 1L, "FILE-1");
        assertNull(AgentMpcTaskRequestProjector.toRequest(null, recipient));

        MpcTaskCommand command = new MpcTaskCommand(
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null);

        MpcTaskCreateRequest request =
                AgentMpcTaskRequestProjector.toRequest(command, recipient);

        assertNull(request.compileParameters());
        assertNull(request.runtimeParameters());
        assertNull(request.taskType());
        assertNull(request.status());
        assertNull(request.partInfo());
    }
}
