package DavexBase.compute.garnet;

import DavexBase.compute.adapter.DockerComputeCommand;

import java.util.Objects;

/**
 * Garnet编译命令及其生成的程序名称。
 * mpcName会在编译成功后保存到任务中，供运行阶段使用。
 */
public record GarnetPreparedCommand(
        DockerComputeCommand command,
        String mpcName) {

    public GarnetPreparedCommand {
        Objects.requireNonNull(command, "command 不能为空");
        if (mpcName == null || mpcName.isBlank()) {
            throw new IllegalArgumentException("mpcName 不能为空");
        }
    }
}