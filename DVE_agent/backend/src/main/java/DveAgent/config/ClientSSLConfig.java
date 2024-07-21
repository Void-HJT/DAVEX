package DveAgent.config;

import javax.net.ssl.KeyManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import DveAgent.common.My;
import io.netty.handler.ssl.SslContextBuilder;

@Configuration
public class ClientSSLConfig {
    @Autowired
    private My my;

    @Bean
    public SslContextBuilder clientSSL() throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(my.getKeyStore(), my.getKeyStorePassword().toCharArray());
        return SslContextBuilder.forClient().keyManager(keyManagerFactory);
    }
}
