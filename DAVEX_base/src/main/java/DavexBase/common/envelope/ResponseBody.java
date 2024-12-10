package DavexBase.common.envelope;

import lombok.Data;

@Data
public class ResponseBody {
    private int statusCode;
    private String message;
    private Object data; // This can be null for failure responses

    public ResponseBody(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    // Getters and Setters
}
