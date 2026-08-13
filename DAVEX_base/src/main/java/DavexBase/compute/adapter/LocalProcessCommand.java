package DavexBase.compute.adapter;

import DavexBase.compute.model.ComputeCommand;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 本地进程执行命令。
 * 保存进程参数和工作目录，不包含具体业务类型的参数。
 */
public record LocalProcessCommand(
        String taskId,
        Duration timeout,
        List<String> arguments,
        Path workingDirectory) implements ComputeCommand {

    public LocalProcessCommand {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        Objects.requireNonNull(timeout, "timeout 不能为空");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout 必须大于 0");
        }
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("进程参数不能为空");
        }
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(workingDirectory, "workingDirectory 不能为空");
    }
}