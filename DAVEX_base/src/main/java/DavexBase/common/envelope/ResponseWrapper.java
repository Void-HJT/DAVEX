package DavexBase.common.envelope;

import lombok.Data;

@Data
public class ResponseWrapper {
    private ResponseEnvelope responseEnvelope;  // 注意：这里的字段名称应与 JSON 中的字段名称保持一致
    // Getters and Setters
}