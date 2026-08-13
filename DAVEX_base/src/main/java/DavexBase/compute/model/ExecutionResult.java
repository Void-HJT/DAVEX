package DavexBase.compute.model;

/**
 * 外部计算结束后的统一结果，包括状态、退出码和进程输出。
 */
public record ExecutionResult(
        ExecutionId executionId,
        ExecutionStatus status,
        Integer exitCode,
        String stdout,
        String stderr) {
}