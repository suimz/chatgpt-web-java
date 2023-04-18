package com.suimz.open.chatgptweb.java.core.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Asynchronous task thread pool configuration
 *
 * @author https://github.com/suimz
 */
@Configuration
public class AsyncTaskPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(executor.getCorePoolSize() * 10);
        executor.setMaxPoolSize(executor.getCorePoolSize() * 10);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AsyncTask-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
