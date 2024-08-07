package DveAgent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:external-databases.yml")
public class ExternalDatabaseConfig {

    @Bean
    public ExternalDatabaseProperties externalDatabaseProperties() {
        return new ExternalDatabaseProperties();
    }
}
