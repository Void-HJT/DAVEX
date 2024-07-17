package DveAgent.common;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import DveAgent.config.SslProperties;

@Component
public class My {
    @Value("${version}")
    private String version;

    @Value("${my.id}")
    private int id;

    @Value("${my.ip}")
    private String ip;

    @Value("${server.port}")
    private int port;

    @Autowired
    private SslProperties sslProperties;

    public SslProperties getSslProperties() {
        return sslProperties;
    }

    public void setSslProperties(SslProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    private PrivateKey privateKey;

    private Certificate certificate;

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    @Autowired
    ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {

        try (InputStream inputStream = resourceLoader.getResource(sslProperties.getKeyStore()).getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance(sslProperties.getKeyStoreType());
            keyStore.load(inputStream, sslProperties.getKeyStorePassword().toCharArray());
            Key key = keyStore.getKey(sslProperties.getKeyAlias(), sslProperties.getKeyStorePassword().toCharArray());
            if (key instanceof PrivateKey) {
                privateKey = (PrivateKey) key;
                certificate = keyStore.getCertificate(sslProperties.getKeyAlias());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keys from keystore", e);
        }
    }

    /**
     * 签名数据
     * 
     * @param data 待签名数据
     * @return 签名后的字符串
     * @throws Exception
     */
    public String signData(byte[] data) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }

    public String getVersion() {
        return version;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
