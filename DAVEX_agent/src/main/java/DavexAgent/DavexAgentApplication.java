package DavexAgent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@MapperScan("DavexBase.mapper")
@ComponentScan(basePackages = {"DavexAgent", "DavexBase"})
@EnableAsync
public class DavexAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DavexAgentApplication.class, args);
        log.info("hello world");
    }
}
