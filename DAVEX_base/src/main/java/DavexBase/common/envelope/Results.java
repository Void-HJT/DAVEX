package DavexBase.common.envelope;

import lombok.Data;

import java.util.Map;

// Results.java
@Data
public class Results {
    private String type;
    private Map<String, Object> setting;

    // Getters and Setters
}
