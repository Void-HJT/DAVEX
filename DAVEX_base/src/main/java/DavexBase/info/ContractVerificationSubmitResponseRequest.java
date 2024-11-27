package DavexBase.info;

import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.*;
import java.util.UUID;
import java.security.Signature;
import java.util.Base64;

@Data
public class ContractVerificationSubmitResponseRequest {
    private String responseID;
    private String requestID;
    private String timeStamp; //时间戳单位为ms
    private String certificateID;

    // 响应签名
    private String responseSignature;
    private String responseMsgHash;
    //请求hash，base64编码
    private String requestHash;
    private String publicKey;

    // 生成响应的示例数据
    public static ContractVerificationSubmitResponseRequest generateResponse(String requestHash, PrivateKey privateKey, String requestMsg) throws Exception {
        ContractVerificationSubmitResponseRequest response = new ContractVerificationSubmitResponseRequest();

        // 随机生成 responseID 和 requestID
        response.setResponseID(UUID.randomUUID().toString());
        response.setRequestID(UUID.randomUUID().toString());

        // 设置当前时间戳
        response.setTimeStamp(String.valueOf(System.currentTimeMillis()));

        // 随机生成 certificateID
        response.setCertificateID(UUID.randomUUID().toString());

        // 计算 requestHash（假设请求数据已传入）
        response.setRequestHash(requestHash);

        // 生成响应消息的哈希值
        String messageToHash = generateHash(requestMsg);
        response.setResponseMsgHash(messageToHash);

        // 使用私钥生成响应签名
        response.setResponseSignature(generateSignature(messageToHash, privateKey));

        return response;
    }

    // 使用 SHA-256 生成 Base64 编码的哈希值
    private static String generateHash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    // 使用指定的 SHA256withRSA 算法生成签名
    private static String generateSignature(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    // 验证签名的方法
    private static boolean verifySignature(String data, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }
}
