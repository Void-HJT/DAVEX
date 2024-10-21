package DavexBase.common.envelope;

import lombok.Data;

import java.util.Map;

// Sharing.java
@Data
public class Sharing {
    private String type;
    private Map<String, Object> setting;

    // Getters and Setters
}
