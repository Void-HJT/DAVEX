package DavexBase.common.envelope;

import lombok.Data;

import java.util.Map;

// Audit.java
@Data
public class Audit {
    private String type;
    private Map<String, Object> setting;

    // Getters and Setters
}
