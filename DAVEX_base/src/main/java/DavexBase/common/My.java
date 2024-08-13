package DavexBase.common;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import DavexBase.entity.Agent;
import DavexBase.entity.Center;
import nl.altindag.ssl.SSLFactory;

@Component
public class My {

    public enum DavexType {
        DAVEX_AGENT,
        DAVEX_CENTER
    }

    @Value("${version}")
    protected String version;

    @Value("${my.id}")
    protected Long id;

    @Value("${my.name}")
    protected String name;

    @Value("${my.description}")
    protected String description;

    @Value("${my.ip}")
    protected String ip;

    @Value("${server.port}")
    protected int port;

    // @Value("${ssl.keystore-path}")
    // protected String keyStorePath;

    // @Value("${ssl.keystore-password}")
    // protected String keyStorePassword;

    // @Value("${ssl.truststore-path}")
    // protected String trustStorePath;

    // @Value("${ssl.truststore-password}")
    // protected String trustStorePassword;

    // @Value("${ssl.key-store-type}")
    // protected String keyStoreType;

    @Value("${my.garnet_path}")
    protected String garnet_path;

    @Value("${my.base_path}")
    protected String base_path;

    @Value("${my.davex_type}")
    protected DavexType davexType;

    private Object myObject;

    protected KeyStore keyStore;

    protected KeyStore trustStore;

    protected PrivateKey privateKey;

    protected Certificate certificate;

    protected SSLFactory baseSslFactory;

    public Object getMyObject() {
        return myObject;
    }

    @PostConstruct
    public void init() throws Exception {
        switch (davexType) {
            default:
            case DAVEX_AGENT:
                myObject = new Agent();
                ((Agent) myObject).setUid(id);
                ((Agent) myObject).setName(name);
                ((Agent) myObject).setIp(ip);
                ((Agent) myObject).setPort(port);
                ((Agent) myObject).setDescription(description);
                ((Agent) myObject).setLastUpdated(LocalDateTime.now());
                break;
            case DAVEX_CENTER:
                myObject = new Center();
                ((Center) myObject).setUid(id);
                ((Center) myObject).setName(name);
                ((Center) myObject).setIp(ip);
                ((Center) myObject).setPort(port);
                ((Center) myObject).setDescription(description);
                ((Center) myObject).setLastUpdated(LocalDateTime.now());
                break;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setId(Long id) {
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

    // public String getKeyStorePath() {
    //     return keyStorePath;
    // }

    // public void setKeyStorePath(String keyStorePath) {
    //     this.keyStorePath = keyStorePath;
    // }

    // public String getKeyStorePassword() {
    //     return keyStorePassword;
    // }

    // public void setKeyStorePassword(String keyStorePassword) {
    //     this.keyStorePassword = keyStorePassword;
    // }

    // public String getTrustStorePath() {
    //     return trustStorePath;
    // }

    // public void setTrustStorePath(String trustStorePath) {
    //     this.trustStorePath = trustStorePath;
    // }

    // public String getTrustStorePassword() {
    //     return trustStorePassword;
    // }

    // public void setTrustStorePassword(String trustStorePassword) {
    //     this.trustStorePassword = trustStorePassword;
    // }

    // public String getKeyStoreType() {
    //     return keyStoreType;
    // }

    // public void setKeyStoreType(String keyStoreType) {
    //     this.keyStoreType = keyStoreType;
    // }

    public String getGarnet_path() {
        return garnet_path;
    }

    public void setGarnet_path(String garnet_path) {
        this.garnet_path = garnet_path;
    }

    public String getBase_path() {
        return base_path;
    }

    public void setBase_path(String base_path) {
        this.base_path = base_path;
    }

    public DavexType getDavexType() {
        return davexType;
    }

    public void setDavexType(DavexType davexType) {
        this.davexType = davexType;
    }
}
