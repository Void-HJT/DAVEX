package DavexBase.compute.port;

import DavexBase.compute.model.ComputeCommand;
import DavexBase.compute.model.ExecutionId;
import DavexBase.compute.model.ExecutionResult;
import DavexBase.compute.model.ExecutionStatus;

/**
 * 统一管理外部计算准备、启动、查询、取消和结果收集的端口。
 *
 * @param <C> 当前运行时使用的命令类型
 */
public interface ComputeExecutor<C extends ComputeCommand> {

    void prepare(C command);

    ExecutionId start(C command);

    ExecutionStatus status(ExecutionId executionId);

    void cancel(ExecutionId executionId);

    ExecutionResult collect(ExecutionId executionId);
}