package DavexBase.task.command;

/**
 * MPC 命令中的参与方及其输入。
 */
public record ParticipantInput(
        String agentId,
        Long part,
        String fileId) {
}
