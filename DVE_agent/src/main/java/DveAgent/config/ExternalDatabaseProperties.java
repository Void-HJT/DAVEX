package DveAgent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "external-databases")
public class ExternalDatabaseProperties {

    private List<DatabaseConfig> databases;

    public static class DatabaseConfig {
        private String name;
        private String type;
        private String url;
        private String username;
        private String password;
        // getters and setters
    }

    // getters and setters
}
