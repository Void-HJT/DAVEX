package DveAgent.common;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.entity.Agent;
import DveAgent.mapper.AgentMapper;
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

    @Value("${my.garnet_path}")
    private String garnet_path;

    private Agent agent;

    private KeyStore keyStore;

    private KeyStore trustStore;

    private PrivateKey privateKey;

    private Certificate certificate;

    private SSLFactory baseSslFactory;

    @Autowired
    private AgentMapper agentMapper;

    @PostConstruct
    public void init() throws Exception {
        LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, id);
        Agent old_agent = agentMapper.selectOne(queryWrapper);
        agent = new Agent();
        agent.setUid(id);
        agent.setName(name);
        agent.setIp(ip);
        agent.setPort(port);
        agent.setDescription(description);
        agent.setLastUpdated(LocalDateTime.now());
        if (old_agent == null || !old_agent.equals(agent)) {
            agentMapper.insert(agent);
        } else {
            agent = old_agent;
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

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
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

    public String getGarnet_path() {
        return garnet_path;
    }

    public void setGarnet_path(String garnet_path) {
        this.garnet_path = garnet_path;
    }

}
