package DavexBase.common;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Data
@Component
public class CertificateGenerator {

    /**
     * 执行 OpenSSL 命令生成私钥和证书
     *
     * @param keyFilePath 输出的私钥文件路径
     * @param certFilePath 输出的证书文件路径
     * @param subject 证书的主体，例如 "agent" 或 "center"
     */
    public void generateKeyAndCertificate(String keyFilePath, String certFilePath, String subject) throws Exception {
        // 固定信息
        String organization = "DSG";
        String organizationalUnit = "FDU";
        String locality = "YANGPU";
        String state = "SHANGHAI";
        String country = "CN";

        // 第一步：生成私钥
        String generateKeyCommand = String.format("openssl genrsa -out %s 3072", keyFilePath);

        // 第二步：生成证书
        String generateCertCommand = String.format(
                "openssl req -x509 -new -nodes -key %s -sha256 -days 3650 -subj \"/C=%s/ST=%s/L=%s/O=%s/OU=%s/CN=%s\" -out %s",
                keyFilePath, country, state, locality, organization, organizationalUnit, subject, certFilePath
        );

        // 执行生成私钥的命令
        executeCommand(generateKeyCommand);

        // 执行生成证书的命令
        executeCommand(generateCertCommand);
    }

    /**
     * 执行终端命令
     *
     * @param command 终端命令
     */
    private void executeCommand(String command) throws Exception {
        System.out.println("执行命令: " + command);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        Process process = processBuilder.start();

        // 读取终端输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // 检查命令执行是否成功
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("命令执行失败，退出码: " + exitCode);
        }
    }

//    public static void main(String[] args) {
//        try {
//            // 为 agent 生成私钥和证书
//            generateKeyAndCertificate("agent.key", "agent.crt", "agent");
//
//            // 为 center 生成私钥和证书
//            generateKeyAndCertificate("center.key", "center.crt", "center");
//
//            System.out.println("私钥和证书生成成功！");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}

