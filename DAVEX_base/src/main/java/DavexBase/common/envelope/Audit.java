package DavexBase.common.envelope;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

// Audit.java
@Data
public class Audit {
    public enum AuditType {
        STORE_PROOF, TRACE
    }

    private AuditType type;
    private JSONObject setting;

    // Getters and Setters
}
