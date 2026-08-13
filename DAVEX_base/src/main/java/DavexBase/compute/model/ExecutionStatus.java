package DavexBase.compute.model;

/**
 * 外部进程或容器的执行状态，不等同于业务任务状态。
 */
public enum ExecutionStatus {
    PREPARING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    TIMED_OUT
}