package DveAgent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import DveAgent.config.SslProperties;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@EnableConfigurationProperties(SslProperties.class)
public class DveAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DveAgentApplication.class, args);
        log.info("hello world");
    }
}
