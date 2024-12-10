package DavexBase.common.envelope;

import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Data;

// Envelope.java
@Data
public class RequestEnvelope {
    private Header header;
    private RequestBody body;

    @JsonIgnore
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    // 生成 Hash
    public void generateHash() throws NoSuchAlgorithmException, JsonProcessingException {
        if (header != null) {
            header.generateHash_Request(this.body);
        }
    }

    // 验证 Hash
    public boolean verifyHash() throws NoSuchAlgorithmException, JsonProcessingException {
        if (header != null) {
            return header.verifyHash_Request(this.body);
        }
        return false;
    }
}
