package DavexBase.common.envelope;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

// Results.java
@Data
public class Results {
    public enum RusultType {
        COMMON_FILE, MPC_FILE, QUERY_FILE, COMPARISON_FILE, FEDERATED_LEARNING_FILE
    }

    private RusultType type;
    private JSONObject setting;

    // Getters and Setters
}
