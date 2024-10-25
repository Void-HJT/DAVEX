package DavexBase.common.envelope;

import lombok.Data;

// Body.java
@Data
public class RequestBody {
    private Authentication authentication;
    private Sharing sharing;
    private Results results;
    private Audit audit;

    // Getters and Setters
}
