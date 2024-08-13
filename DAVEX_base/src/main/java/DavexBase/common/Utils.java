package DavexBase.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

public class Utils {

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

    public static Path resolveFileNameConflict(Path filePath) {
        int counter = 1;
        String fileName = filePath.getFileName().toString();
        String fileNameWithoutExt = fileName;
        String extension = "";

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileNameWithoutExt = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        while (Files.exists(filePath)) {
            String newFileName = fileNameWithoutExt + "_" + counter + extension;
            filePath = filePath.getParent().resolve(newFileName);
            counter++;
        }

        return filePath;
    }

    public static int hashStringToInt(String input) {
        try {
            // 使用SHA-256算法来生成散列值
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // 取散列值的前4个字节，并转换为一个整数
            return ByteBuffer.wrap(hash).getInt();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such hashing algorithm", e);
        }
    }

    public static String getFileHash(FileSystemResource fileResource, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        try (InputStream is = new FileInputStream(fileResource.getFile());
                DigestInputStream dis = new DigestInputStream(is, digest)) {
            while (dis.read() != -1)
                ;// 空读
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean verifyMultipartFileHash(MultipartFile multipartFile, String expectedHash, String algorithm) {
        try {
            String fileHash = getMultipartFileHash(multipartFile, algorithm);
            return fileHash.equalsIgnoreCase(expectedHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getMultipartFileHash(MultipartFile multipartFile, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = inputStream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(String.format("%02x", aByte));
        }

        return sb.toString();
    }
}
