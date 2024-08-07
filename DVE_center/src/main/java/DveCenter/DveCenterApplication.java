package DveCenter;

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
@MapperScan("DveBase.mapper")
@MapperScan("DveCenter.mapper")
@ComponentScan(basePackages = { "DveCenter", "DveBase" })
@EnableAsync
public class DveCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(DveCenterApplication.class, args);
        log.info("hello world");
    }
}
