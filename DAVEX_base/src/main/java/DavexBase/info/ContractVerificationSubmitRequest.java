package DavexBase.info;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.UUID;

@Data
public class ContractVerificationSubmitRequest {
    private String requestID;
    private String timeStamp; //单位都是ms
    private String fileDescription;
    private String sharingSettingHash;
    private String certificateID;
    private String publicKey;
    private String requestSignature;
    private String requestMsgHash;

    // 生成请求的示例数据
    public static ContractVerificationSubmitRequest generateRequest(String fileDescription, PrivateKey privateKey, String requestMsg) throws Exception {
        ContractVerificationSubmitRequest request = new ContractVerificationSubmitRequest();

        // 生成唯一的 requestID 和 certificateID
        request.setRequestID(UUID.randomUUID().toString());
        request.setCertificateID(UUID.randomUUID().toString());

        // 设置当前时间戳
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));

        // 设置文件描述
        request.setFileDescription(fileDescription);

        // 假设 sharingSettingHash 是一个静态哈希值（根据需求替换为真实数据）
        request.setSharingSettingHash(generateHash("SharingSettingExampleData"));

        // 生成请求消息的哈希值
        String messageToHash = generateHash(requestMsg);
        request.setRequestMsgHash(messageToHash);

        // 使用私钥生成签名
        request.setRequestSignature(generateSignature(messageToHash, privateKey));

        return request;
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
    public static boolean verifySignature(String data, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }
}
