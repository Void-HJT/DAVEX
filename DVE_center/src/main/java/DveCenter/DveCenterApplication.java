package DveCenter;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@MapperScan("DveAgent.mapper")
@MapperScan("DveCenter.mapper")
@EnableAsync
public class DveCenterApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(DveCenterApplication.class,args);
        log.info("hello world");
    }
}
