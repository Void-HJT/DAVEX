package DavexBase.service.auth;

/**
 * 表示 Center 与 Agent 节点通信过程中发生的统一基础设施异常。
 */
public final class NodeCommunicationException extends RuntimeException {

    public enum Reason {
        TIMEOUT,
        UNREACHABLE,
        HTTP_ERROR
    }

    private final Reason reason;
    private final String nodeId;
    private final String operation;
    private final Integer statusCode;

    private NodeCommunicationException(
            Reason reason,
            String nodeId,
            String operation,
            Integer statusCode,
            String message,
            Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.nodeId = nodeId;
        this.operation = operation;
        this.statusCode = statusCode;
    }

    /**
     * 创建节点响应超时异常。
     */
    static NodeCommunicationException timeout(
            String nodeId,
            String operation,
            Throwable cause) {
        return new NodeCommunicationException(
                Reason.TIMEOUT,
                nodeId,
                operation,
                null,
                "节点响应超时：" + nodeId + "，操作：" + operation,
                cause);
    }

    /**
     * 创建节点不可达或底层连接失败异常。
     */
    static NodeCommunicationException unreachable(
            String nodeId,
            String operation,
            Throwable cause) {
        return new NodeCommunicationException(
                Reason.UNREACHABLE,
                nodeId,
                operation,
                null,
                "节点不可达：" + nodeId + "，操作：" + operation,
                cause);
    }

    /**
     * 创建远端 HTTP 错误响应异常。
     */
    static NodeCommunicationException httpError(
            String nodeId,
            String operation,
            int statusCode,
            Throwable cause) {
        return new NodeCommunicationException(
                Reason.HTTP_ERROR,
                nodeId,
                operation,
                statusCode,
                "节点返回HTTP错误：" + statusCode
                        + "，节点：" + nodeId
                        + "，操作：" + operation,
                cause);
    }

    public Reason getReason() {
        return reason;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getOperation() {
        return operation;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}