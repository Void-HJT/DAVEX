package DavexCenter.module.task.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;
import DavexBase.task.command.TaskOptions;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

/**
 * 为指定 Agent 生成 MPC 创建请求。
 *
 * 每个 Agent 只收到自己的输入文件 ID；
 * 参与方列表中的文件 ID 全部脱敏。
 */
public final class AgentMpcTaskRequestProjector {

    private AgentMpcTaskRequestProjector() {
    }

    public static MpcTaskCreateRequest toRequest(
            MpcTaskCommand command,
            ParticipantInput recipient) {

        if (command == null) {
            return null;
        }

        TaskOptions options = command.options();
        Map<String, Object> compileParameters =
                options == null ? null : options.compileParameters();
        Map<String, Object> runtimeParameters =
                options == null ? null : options.runtimeParameters();

        return new MpcTaskCreateRequest(
                command.uid(),
                command.applicationId(),
                command.centerId(),
                command.mpcId(),
                compileParameters,
                runtimeParameters,
                command.n(),
                recipient.part(),
                command.host(),
                command.port(),
                recipient.fileId(),
                toTaskType(command.taskType()),
                toStatus(command.status()),
                maskedParticipants(command.participants()));
    }

    private static List<MpcTaskCreateRequest.PartInfo> maskedParticipants(
            List<ParticipantInput> participants) {

        if (participants == null) {
            return null;
        }

        List<MpcTaskCreateRequest.PartInfo> result = new ArrayList<>();

        for (ParticipantInput participant : participants) {
            result.add(new MpcTaskCreateRequest.PartInfo(
                    participant.agentId(),
                    participant.part(),
                    null));
        }

        return result;
    }

    private static MpcTaskCreateRequest.TaskType toTaskType(
            MpcTaskCommand.TaskType taskType) {

        return taskType == null
                ? null
                : MpcTaskCreateRequest.TaskType.valueOf(
                        taskType.name());
    }

    private static MpcTaskCreateRequest.Status toStatus(
            MpcTaskCommand.Status status) {

        return status == null
                ? null
                : MpcTaskCreateRequest.Status.valueOf(
                        status.name());
    }
}
