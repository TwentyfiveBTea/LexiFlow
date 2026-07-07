package com.btea.lexiflow.infrastructure.async.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/7 15:00
 * @Description: 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("articleTaskExecutor")
    public Executor articleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("article-task-");
        executor.initialize();
        return executor;
    }
}
