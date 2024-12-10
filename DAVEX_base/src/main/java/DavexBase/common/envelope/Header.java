package DavexBase.common.envelope;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Data;

// Header.java
@Data
public class Header {
    private String version;
    private String timeStamp;
    private String hash;
    private UUID requestId;
    private String sender;
    private String receiver;

    @JsonIgnore
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    // 生成不包括 hashing 字段的hash
    public void generateHash_Request(RequestBody body) throws NoSuchAlgorithmException, JsonProcessingException {
        Header headerCopy = this.cloneWithoutHash();
        String headerJson = objectMapper.writeValueAsString(headerCopy);
        String bodyJson = objectMapper.writeValueAsString(body);
        String combinedJson = headerJson + bodyJson;
        this.hash = calculateHash(combinedJson);
    }

    public void generateHash_Response(ResponseBody body) throws NoSuchAlgorithmException, JsonProcessingException {
        Header headerCopy = this.cloneWithoutHash();
        String headerJson = objectMapper.writeValueAsString(headerCopy);
        String bodyJson = objectMapper.writeValueAsString(body);
        String combinedJson = headerJson + bodyJson;
        this.hash = calculateHash(combinedJson);
    }

    // 验证哈希
    public boolean verifyHash_Request(RequestBody body) throws NoSuchAlgorithmException, JsonProcessingException {
        String originalHash = this.hash;
        this.generateHash_Request(body);
        return originalHash != null && originalHash.equals(this.hash);
    }

    public boolean verifyHash_Response(ResponseBody body) throws NoSuchAlgorithmException, JsonProcessingException {
        String originalHash = this.hash;
        this.generateHash_Response(body);
        return originalHash != null && originalHash.equals(this.hash);
    }

    private Header cloneWithoutHash() {
        Header headerCopy = new Header();
        headerCopy.version = this.version;
        headerCopy.timeStamp = this.timeStamp;
        headerCopy.requestId = this.requestId;
        headerCopy.sender = this.sender;
        headerCopy.receiver = this.receiver;
        return headerCopy;
    }

    // 计算哈希
    private String calculateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
