package DavexBase.common.envelope;

import lombok.Data;

// Authentication.java
@Data
public class Authentication {
    private String sender;
    private String receiver;
    private String token;

    // Getters and Setters
}
