package DavexBase.common.envelope;

import lombok.Data;

// Envelope.java
@Data
public class RequestEnvelope {
    private Header header;
    private RequestBody body;
    // Getters and Setters
}
