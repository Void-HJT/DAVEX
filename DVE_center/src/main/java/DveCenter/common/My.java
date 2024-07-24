package DveCenter.common;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import DveAgent.common.Utlis;
import DveAgent.entity.Center;
import nl.altindag.ssl.SSLFactory;

@Component
public class My {
    @Value("${version}")
    private String version;

    @Value("${my.id}")
    private long id;

    @Value("${my.name}")
    private String name;

    @Value("${my.description}")
    private String description;

    @Value("${my.ip}")
    private String ip;

    @Value("${server.port}")
    private int port;

    @Value("${ssl.keystore-path}")
    private String keyStorePath;

    @Value("${ssl.keystore-password}")
    private String keyStorePassword;

    @Value("${ssl.truststore-path}")
    private String trustStorePath;

    @Value("${ssl.truststore-password}")
    private String trustStorePassword;

    @Value("${ssl.key-store-type}")
    private String keyStoreType;

    private Center center;

    private KeyStore keyStore;

    private KeyStore trustStore;

    private PrivateKey privateKey;

    private Certificate certificate;

    private SSLFactory baseSslFactory;

    @PostConstruct
    public void init() throws Exception {

        try (InputStream inputStream = new ClassPathResource(keyStorePath).getInputStream()) {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(inputStream, keyStorePassword.toCharArray());
            Key key = keyStore.getKey(name, keyStorePassword.toCharArray());
            if (key instanceof PrivateKey) {
                privateKey = (PrivateKey) key;
                certificate = keyStore.getCertificate(name);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keys from keystore", e);
        }

        trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        try (InputStream inputStream = new ClassPathResource(trustStorePath).getInputStream()) {
            trustStore = KeyStore.getInstance(keyStoreType);
            trustStore.load(inputStream,
                    trustStorePassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load trustStore", e);
        }
        baseSslFactory = SSLFactory.builder()
                .withIdentityMaterial(keyStore, keyStorePassword.toCharArray())
                .withSwappableIdentityMaterial().withSwappableTrustMaterial()
                .withTrustMaterial(trustStore)
                .build();
        center = new Center();
        center.setUid(id);
        center.setName(name);
        center.setIp(ip);
        center.setPort(port);
        center.setDescription(description);
        center.setCrt(Utlis.certificateToBytes(certificate));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SSLFactory getBaseSslFactory() {
        return baseSslFactory;
    }

    public void setBaseSslFactory(SSLFactory baseSslFactory) {
        this.baseSslFactory = baseSslFactory;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public String getVersion() {
        return version;
    }

    public long getId() {
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

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

}
