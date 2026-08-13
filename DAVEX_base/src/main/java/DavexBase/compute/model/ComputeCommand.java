package DavexBase.compute.model;

import java.time.Duration;

/**
 * 外部计算命令的最小公共接口。
 * 各运行时可以保留自己的业务参数，只需提供任务标识和执行超时。
 */
public interface ComputeCommand {

    String taskId();

    Duration timeout();
}