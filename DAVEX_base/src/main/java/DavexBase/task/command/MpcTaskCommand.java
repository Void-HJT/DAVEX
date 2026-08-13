package DavexBase.task.command;

import java.util.List;

/**
 * 应用内部执行 MPC 任务时使用的命令。
 *
 * 不属于网络协议，也不是数据库实体。
 */
public record MpcTaskCommand(
        String uid,
        String applicationId,
        String centerId,
        String mpcId,
        TaskOptions options,
        Integer n,
        Long part,
        String host,
        Integer port,
        String dataId,
        TaskType taskType,
        Status status,
        List<ParticipantInput> participants) {

    public MpcTaskCommand {
        participants = participants == null
                ? null
                : List.copyOf(participants);
    }

    /**
     * 返回仅替换任务参数的命令副本。
     */
    public MpcTaskCommand withOptions(TaskOptions newOptions) {
        return new MpcTaskCommand(
                uid,
                applicationId,
                centerId,
                mpcId,
                newOptions,
                n,
                part,
                host,
                port,
                dataId,
                taskType,
                status,
                participants);
    }

    /**
     * 返回仅替换任务 UID 的命令副本。
     */
    public MpcTaskCommand withUid(String newUid) {
        return new MpcTaskCommand(
                newUid,
                applicationId,
                centerId,
                mpcId,
                options,
                n,
                part,
                host,
                port,
                dataId,
                taskType,
                status,
                participants);
    }

    /**
     * 返回替换 Center 本地输入信息的命令副本。
     */
    public MpcTaskCommand withInput(
            String newDataId,
            String newHost) {

        return new MpcTaskCommand(
                uid,
                applicationId,
                centerId,
                mpcId,
                options,
                n,
                part,
                newHost,
                port,
                newDataId,
                taskType,
                status,
                participants);
    }

    public enum TaskType {
        GARNET_MPC,
        GARNET_PSI,
        GARNET_INFERENCE
    }

    public enum Status {
        INIT,
        COMPILING,
        READY,
        RUNNING,
        FINISHED,
        FAILED
    }
}
