package DavexBase.contract;

import java.util.ArrayList;
import java.util.List;

import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

/**
 * 将 MPC 创建通信契约映射为内部应用命令。
 */
public final class MpcTaskCreateRequestMapper {

    private MpcTaskCreateRequestMapper() {
    }

    public static MpcTaskCommand toCommand(
            MpcTaskCreateRequest request) {

        if (request == null) {
            return null;
        }

        return new MpcTaskCommand(
                request.uid(),
                request.applicationId(),
                request.centerId(),
                request.mpcId(),
                new TaskOptions(
                        request.compileParameters(),
                        request.runtimeParameters()),
                request.n(),
                request.part(),
                request.host(),
                request.port(),
                request.dataId(),
                toTaskType(request.taskType()),
                toStatus(request.status()),
                toParticipants(request.partInfo())
        );
    }

    private static MpcTaskCommand.TaskType toTaskType(
            MpcTaskCreateRequest.TaskType taskType) {

        return taskType == null
                ? null
                : MpcTaskCommand.TaskType.valueOf(taskType.name());
    }

    private static MpcTaskCommand.Status toStatus(
            MpcTaskCreateRequest.Status status) {

        return status == null
                ? null
                : MpcTaskCommand.Status.valueOf(status.name());
    }

    private static List<ParticipantInput> toParticipants(
            List<MpcTaskCreateRequest.PartInfo> parts) {

        if (parts == null) {
            return null;
        }

        List<ParticipantInput> result = new ArrayList<>();

        for (MpcTaskCreateRequest.PartInfo part : parts) {
            result.add(new ParticipantInput(
                    part.agentID(),
                    part.part(),
                    part.fileID()));
        }

        return result;
    }
}
