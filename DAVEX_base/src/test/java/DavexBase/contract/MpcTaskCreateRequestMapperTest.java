package DavexBase.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import DavexBase.entity.MpcTask;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

class MpcTaskCreateRequestMapperTest {

    @Test
    void mapsEveryContractFieldToIndependentCommandModel() {
        Map<String, Object> compileParameters = new LinkedHashMap<>();
        compileParameters.put("prime", 128);
        Map<String, Object> runtimeParameters = new LinkedHashMap<>();
        runtimeParameters.put("batch", 32);

        MpcTaskCreateRequest request = new MpcTaskCreateRequest(
                "TASK-1",
                "APPLICATION-1",
                "CENTER-1",
                "MPC-1",
                compileParameters,
                runtimeParameters,
                2,
                1L,
                "127.0.0.1",
                9000,
                "FILE-1",
                MpcTaskCreateRequest.TaskType.GARNET_MPC,
                MpcTaskCreateRequest.Status.READY,
                List.of(new MpcTaskCreateRequest.PartInfo(
                        "AGENT-1", 1L, "FILE-1")));

        MpcTaskCommand command =
                MpcTaskCreateRequestMapper.toCommand(request);

        assertEquals("TASK-1", command.uid());
        assertEquals("APPLICATION-1", command.applicationId());
        assertEquals("CENTER-1", command.centerId());
        assertEquals("MPC-1", command.mpcId());
        assertEquals(128, command.options().compileParameters().get("prime"));
        assertEquals(32, command.options().runtimeParameters().get("batch"));
        assertEquals(2, command.n());
        assertEquals(1L, command.part());
        assertEquals("127.0.0.1", command.host());
        assertEquals(9000, command.port());
        assertEquals("FILE-1", command.dataId());
        assertEquals(MpcTaskCommand.TaskType.GARNET_MPC, command.taskType());
        assertEquals(MpcTaskCommand.Status.READY, command.status());
        assertEquals(
                new ParticipantInput("AGENT-1", 1L, "FILE-1"),
                command.participants().get(0));

        assertFalse(MpcTask.class.isAssignableFrom(MpcTaskCommand.class));
        assertFalse(MpcTaskCreateRequest.class.isAssignableFrom(
                MpcTaskCommand.class));
    }

    @Test
    void copiesAndProtectsMutableRequestCollections() {
        Map<String, Object> compileParameters = new LinkedHashMap<>();
        compileParameters.put("prime", 128);
        Map<String, Object> runtimeParameters = new LinkedHashMap<>();
        runtimeParameters.put("batch", 32);
        List<MpcTaskCreateRequest.PartInfo> participants = new ArrayList<>();
        participants.add(new MpcTaskCreateRequest.PartInfo(
                "AGENT-1", 1L, "FILE-1"));

        MpcTaskCreateRequest request = new MpcTaskCreateRequest(
                "TASK-1", null, null, null,
                compileParameters, runtimeParameters,
                null, null, null, null, null,
                null, null, participants);

        MpcTaskCommand command =
                MpcTaskCreateRequestMapper.toCommand(request);

        compileParameters.put("prime", 256);
        runtimeParameters.clear();
        participants.clear();

        assertEquals(128,
                command.options().compileParameters().get("prime"));
        assertEquals(32,
                command.options().runtimeParameters().get("batch"));
        assertEquals(1, command.participants().size());

        assertThrows(UnsupportedOperationException.class,
                () -> command.options().compileParameters()
                        .put("new", true));
        assertThrows(UnsupportedOperationException.class,
                () -> command.options().runtimeParameters().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> command.participants().clear());
    }

    @Test
    void preservesCurrentNullSemantics() {
        assertNull(MpcTaskCreateRequestMapper.toCommand(null));

        MpcTaskCreateRequest request = new MpcTaskCreateRequest(
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null);

        MpcTaskCommand command =
                MpcTaskCreateRequestMapper.toCommand(request);

        assertNotNull(command.options());
        assertNull(command.options().compileParameters());
        assertNull(command.options().runtimeParameters());
        assertNull(command.taskType());
        assertNull(command.status());
        assertNull(command.participants());
    }

    @Test
    void replacesOptionsWithoutMutatingOriginalCommand() {
        MpcTaskCommand original = new MpcTaskCommand(
                "TASK-1", null, null, null,
                new TaskOptions(Map.of("old", 1), Map.of("keep", 2)),
                null, null, null, null, null,
                null, null,
                List.of(new ParticipantInput("AGENT-1", 1L, "FILE-1")));
        TaskOptions replacement =
                new TaskOptions(Map.of("new", 3), Map.of("keep", 2));

        MpcTaskCommand updated = original.withOptions(replacement);

        assertNotSame(original, updated);
        assertSame(replacement, updated.options());
        assertEquals(1, original.options().compileParameters().get("old"));
        assertEquals("TASK-1", updated.uid());
        assertEquals(original.participants(), updated.participants());
    }

    @Test
    void replacesUidWithoutMutatingOriginalCommand() {
        MpcTaskCommand original = new MpcTaskCommand(
                null, null, null, null, null,
                null, null, null, null, null,
                null, null,
                List.of(new ParticipantInput("AGENT-1", 1L, "FILE-1")));

        MpcTaskCommand updated = original.withUid("TASK-1");

        assertNotSame(original, updated);
        assertNull(original.uid());
        assertEquals("TASK-1", updated.uid());
        assertEquals(original.participants(), updated.participants());
    }

    @Test
    void replacesCenterInputWithoutMutatingOriginalCommand() {
        MpcTaskCommand original = new MpcTaskCommand(
                null, null, null, null, null,
                null, null, "original-host", null, "ORIGINAL-INPUT",
                null, null, null);

        MpcTaskCommand updated =
                original.withInput("INPUT-1", "127.0.0.1");

        assertNotSame(original, updated);
        assertEquals("ORIGINAL-INPUT", original.dataId());
        assertEquals("original-host", original.host());
        assertEquals("INPUT-1", updated.dataId());
        assertEquals("127.0.0.1", updated.host());
    }
}
