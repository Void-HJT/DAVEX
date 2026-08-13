package DavexBase.compute.adapter;

import DavexBase.compute.model.ComputeCommand;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Docker容器内执行命令。
 * 只保存容器执行参数，不包含Garnet等业务参数。
 */
public record DockerComputeCommand(
        String taskId,
        Duration timeout,
        String containerId,
        String workingDirectory,
        List<String> arguments) implements ComputeCommand {

    public DockerComputeCommand {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        Objects.requireNonNull(timeout, "timeout 不能为空");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout 必须大于 0");
        }
        if (containerId == null || containerId.isBlank()) {
            throw new IllegalArgumentException("containerId 不能为空");
        }
        if (workingDirectory == null || workingDirectory.isBlank()) {
            throw new IllegalArgumentException("workingDirectory 不能为空");
        }
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("Docker命令不能为空");
        }
        arguments = List.copyOf(arguments);
    }
}