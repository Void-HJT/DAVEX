package DveAgent.common;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.util.Base64;

public class Utlis {
    /**
     * 将证书对象序列化为Base64字符串
     * 
     * @param certificate 证书对象
     * @return 序列化的证书字符串
     * @throws Exception
     */
    public static String certificateToString(Certificate certificate) throws Exception {
        // 将证书编码为Base64字符串
        byte[] encodedCert = certificate.getEncoded();
        String base64Cert = Base64.getEncoder().encodeToString(encodedCert);
        return base64Cert;
    }

    /**
     * 将Base64字符串解码为证书对象
     * 
     * @param certString 序列化的证书字符串
     * @return 证书对象
     * @throws Exception
     */
    public static Certificate stringToCertificate(String certString) throws Exception {
        // 将Base64字符串解码为字节数组
        byte[] decodedCert = Base64.getDecoder().decode(certString);
        // 生成证书对象
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return certFactory.generateCertificate(new ByteArrayInputStream(decodedCert));
    }

    /**
     * 验证签名
     * 
     * @param data 待验证数据
     * @param signedData 待验证签名
     * @param certificate 证书对象
     * @return 签名是否合法
     * @throws Exception
     */
    public static boolean verifyData(byte[] data, String signedData, Certificate certificate) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate);
        signature.update(data);
        byte[] signedBytes = Base64.getDecoder().decode(signedData);
        return signature.verify(signedBytes);
    }
}
