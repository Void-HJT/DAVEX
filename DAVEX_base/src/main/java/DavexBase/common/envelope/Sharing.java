package DavexBase.common.envelope;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

// Sharing.java
@Data
public class Sharing {
    public enum SharingType {
        FILE_EXCHANGE, COMPARISON, QUERY, PSI, MPC, SECURITY_INFERENCE, FEDERATED_LEARNING
    }

    private SharingType type;
    private JSONObject setting;

    // Getters and Setters
}
