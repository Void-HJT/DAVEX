package DavexAgent.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "customExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(
            @Value("${threadpool.corePoolSize}") int corePoolSize,
            @Value("${threadpool.maxPoolSize}") int maxPoolSize,
            @Value("${threadpool.queueCapacity}") int queueCapacity,
            @Value("${threadpool.keepAliveSeconds}") int keepAliveSeconds,
            @Value("${threadpool.awaitTerminationSeconds}") int awaitTerminationSeconds,
            @Value("${threadpool.threadNamePrefix}") String threadNamePrefix) {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        threadPoolTaskExecutor.setThreadNamePrefix(threadNamePrefix);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
