package DveAgent.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Map;
import java.nio.file.*;

public class Utlis {

    /**
     * 将证书对象序列化为Base64字符串
     * 
     * @param certificate 证书对象
     * @return 序列化的二进制证书
     * @throws Exception
     */
    public static byte[] certificateToBytes(Certificate certificate) throws Exception {
        // 将证书编码为Base64字符串
        byte[] encodedCert = certificate.getEncoded();
        String base64Cert = Base64.getEncoder().encodeToString(encodedCert);
        base64Cert = "-----BEGIN CERTIFICATE-----\n" +
                base64Cert +
                "\n-----END CERTIFICATE-----";
        return base64Cert.getBytes();
    }

    /**
     * 将序列化的二进制证书解码为证书对象
     * 
     * @param cert 序列化的二进制证书
     * @return 证书对象
     * @throws Exception
     */
    public static Certificate bytesToCertificate(byte[] cert) throws Exception {
        // 生成证书对象
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return certFactory.generateCertificate(new ByteArrayInputStream(cert));
    }

    /**
     * 验证签名
     * 
     * @param data        待验证数据
     * @param signedData  待验证签名
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

    public static void replaceStringsInFile(String inputFilePath, String outputFilePath,
            Map<String, String> replacementMap) throws IOException {
        Path inputPath = Paths.get(inputFilePath);
        Path outputPath = Paths.get(outputFilePath);

        try (BufferedReader reader = Files.newBufferedReader(inputPath);
                BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
