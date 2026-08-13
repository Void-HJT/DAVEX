package DavexAgent.module.task.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import DavexBase.entity.MpcTask;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.junit.jupiter.api.Test;

class MpcTaskCommandAssemblerTest {

    @Test
    void mapsCommandFieldsToPersistenceEntity() {
        MpcTaskCommand command = new MpcTaskCommand(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                new TaskOptions(
                        Map.of("prime", 128),
                        Map.of("batch", 32)),
                2,
                1L,
                "127.0.0.1",
                6000,
                "FILE-1",
                MpcTaskCommand.TaskType.GARNET_MPC,
                MpcTaskCommand.Status.READY,
                List.of(new ParticipantInput(
                        "AGENT-1", 1L, "FILE-1")));

        MpcTask entity = MpcTaskCommandAssembler.toEntity(command);

        assertEquals("TASK-1", entity.getUid());
        assertEquals("APPLICATION-1", entity.getApplicationId());
        assertEquals("CENTER-1", entity.getCenterId());
        assertEquals("MPC-1", entity.getMpcId());
        assertEquals(128,
                entity.getCompileParameters().getInteger("prime"));
        assertEquals(32,
                entity.getRuntimeParameters().getInteger("batch"));
        assertEquals(2, entity.getN());
        assertEquals(1L, entity.getPart());
        assertEquals("127.0.0.1", entity.getHost());
        assertEquals(6000, entity.getPort());
        assertEquals("FILE-1", entity.getDataId());
        assertEquals(MpcTask.TaskType.GARNET_MPC,
                entity.getTaskType());
        assertEquals(MpcTask.Status.READY, entity.getStatus());

        entity.getCompileParameters().put("prime", 256);
        assertEquals(128,
                command.options().compileParameters().get("prime"));
    }

    @Test
    void preservesNullableOptionsAndCommand() {
        assertNull(MpcTaskCommandAssembler.toEntity(null));

        MpcTaskCommand command = new MpcTaskCommand(
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null);

        MpcTask entity = MpcTaskCommandAssembler.toEntity(command);

        assertNull(entity.getCompileParameters());
        assertNull(entity.getRuntimeParameters());
        assertNull(entity.getTaskType());
        assertNull(entity.getStatus());
    }
}
