package DavexBase.common.envelope;

import lombok.Data;

// Header.java
@Data
public class Header {
    private String version;
    private String timeStamp;
    private String hash;
    private String requestId;
    private String sender;
    private String receiver;
    // Getters and Setters
}