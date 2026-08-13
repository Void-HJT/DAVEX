package DavexBase.compute.model;

/**
 * 标识一次独立的外部计算执行。
 */
public record ExecutionId(String value) {

    public ExecutionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("executionId 不能为空");
        }
    }
}