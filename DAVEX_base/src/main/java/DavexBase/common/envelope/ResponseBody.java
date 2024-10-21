package DavexBase.common.envelope;

import lombok.Data;

@Data
public class ResponseBody {
    private String type;  // success or failure
    private int statusCode;
    private String message;
    private Object data;   // This can be null for failure responses

    // Getters and Setters
}
