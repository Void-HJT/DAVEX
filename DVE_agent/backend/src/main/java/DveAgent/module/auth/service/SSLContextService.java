package DveAgent.module.auth.service;

import java.security.KeyStore;

import org.springframework.stereotype.Service;

import DveAgent.common.My;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.SSLFactoryUtils;

@Service
public class SSLContextService {

    private My my;

    private final SSLFactory baseSslFactory;

    public SSLContextService(SSLFactory baseSslFactory, My my) {
        this.baseSslFactory = baseSslFactory;
        this.my = my;
    }

    public void configureGlobalSSLContext(KeyStore trustStore, KeyStore keyStore) throws Exception {

        SSLFactory updatedSslFactory = SSLFactory.builder()
                .withIdentityMaterial(my.getKeyStore(), my.getKeyStorePassword().toCharArray())
                .withTrustMaterial(trustStore).build();
        SSLFactoryUtils.reload(baseSslFactory, updatedSslFactory);
    }
}
