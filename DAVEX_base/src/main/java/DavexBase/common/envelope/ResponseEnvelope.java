package DavexBase.common.envelope;

import lombok.Data;

@Data
public class ResponseEnvelope {
    private Header header;
    private ResponseBody body;

    // Getters and Setters
}
