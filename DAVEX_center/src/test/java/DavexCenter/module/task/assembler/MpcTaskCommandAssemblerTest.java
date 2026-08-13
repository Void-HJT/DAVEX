package DavexCenter.module.task.assembler;

import DavexBase.entity.MpcTask;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MpcTaskCommandAssemblerTest {

    @Test
    void mapsCommandToIndependentPersistenceEntity() {
        MpcTaskCommand command = new MpcTaskCommand(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(Map.of("prime", 128), Map.of("batch", 32)),
                2,
                0L,
                "127.0.0.1",
                9000,
                "INPUT-1",
                MpcTaskCommand.TaskType.GARNET_MPC,
                MpcTaskCommand.Status.INIT,
                List.of(new ParticipantInput("AGENT-1", 1L, "FILE-1")));

        MpcTask entity = MpcTaskCommandAssembler.toEntity(command);

        assertEquals("TASK-1", entity.getUid());
        assertEquals("APPLICATION-1", entity.getApplicationId());
        assertEquals("CENTER-1", entity.getCenterId());
        assertEquals("MPC-1", entity.getMpcId());
        assertEquals(2, entity.getN());
        assertEquals(0L, entity.getPart());
        assertEquals("127.0.0.1", entity.getHost());
        assertEquals(9000, entity.getPort());
        assertEquals("INPUT-1", entity.getDataId());
        assertEquals(MpcTask.TaskType.GARNET_MPC, entity.getTaskType());
        assertEquals(MpcTask.Status.INIT, entity.getStatus());
        assertEquals(128, entity.getCompileParameters().get("prime"));
        assertEquals(32, entity.getRuntimeParameters().get("batch"));
    }

    @Test
    void preservesNullCommandAndOptionsSemantics() {
        assertNull(MpcTaskCommandAssembler.toEntity(null));

        MpcTask entity = MpcTaskCommandAssembler.toEntity(
                new MpcTaskCommand(
                        null, null, null, null, null,
                        null, null, null, null, null,
                        null, null, null));

        assertNull(entity.getCompileParameters());
        assertNull(entity.getRuntimeParameters());
        assertNull(entity.getTaskType());
        assertNull(entity.getStatus());
    }
}
