package DveCenter.config;

import nl.altindag.ssl.SSLFactory;

import nl.altindag.ssl.jetty.util.JettySslUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLConfig {

    @Bean
    public SSLFactory sslFactory(@Value("${ssl.keystore-path}") String keyStorePath,
            @Value("${ssl.keystore-password}") char[] keyStorePassword,
            @Value("${ssl.truststore-path}") String trustStorePath,
            @Value("${ssl.truststore-password}") char[] trustStorePassword,
            @Value("${ssl.client-auth}") boolean isClientAuthenticationRequired) {

        return SSLFactory.builder()
                .withSwappableIdentityMaterial()
                .withSwappableTrustMaterial()
                .withIdentityMaterial(keyStorePath, keyStorePassword)
                .withTrustMaterial(trustStorePath, trustStorePassword)
                .withNeedClientAuthentication(isClientAuthenticationRequired)
                .build();
    }

    @Bean
    public SslContextFactory.Server sslContextFactory(SSLFactory sslFactory) {
        return JettySslUtils.forServer(sslFactory);
    }

}