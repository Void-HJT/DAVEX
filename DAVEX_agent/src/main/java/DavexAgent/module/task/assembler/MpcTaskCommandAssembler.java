package DavexAgent.module.task.assembler;

import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import DavexBase.entity.MpcTask;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.TaskOptions;

/**
 * 将 Agent 收到的内部 MPC 命令装配成持久化实体。
 */
public final class MpcTaskCommandAssembler {

    private MpcTaskCommandAssembler() {
    }

    public static MpcTask toEntity(MpcTaskCommand command) {
        if (command == null) {
            return null;
        }

        MpcTask target = new MpcTask();
        target.setUid(command.uid());
        target.setApplicationId(command.applicationId());
        target.setCenterId(command.centerId());
        target.setMpcId(command.mpcId());
        target.setN(command.n());
        target.setPart(command.part());
        target.setHost(command.host());
        target.setPort(command.port());
        target.setDataId(command.dataId());
        target.setTaskType(toTaskType(command.taskType()));
        target.setStatus(toStatus(command.status()));

        TaskOptions options = command.options();
        if (options != null) {
            target.setCompileParameters(
                    toJsonObject(options.compileParameters()));
            target.setRuntimeParameters(
                    toJsonObject(options.runtimeParameters()));
        }

        return target;
    }

    private static JSONObject toJsonObject(
            Map<String, Object> parameters) {

        if (parameters == null) {
            return null;
        }

        return new JSONObject(new LinkedHashMap<>(parameters));
    }

    private static MpcTask.TaskType toTaskType(
            MpcTaskCommand.TaskType taskType) {

        return taskType == null
                ? null
                : MpcTask.TaskType.valueOf(taskType.name());
    }

    private static MpcTask.Status toStatus(
            MpcTaskCommand.Status status) {

        return status == null
                ? null
                : MpcTask.Status.valueOf(status.name());
    }
}
