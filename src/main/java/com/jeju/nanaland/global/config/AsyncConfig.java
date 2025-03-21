package com.jeju.nanaland.global.config;

import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
  @Override
  @Bean(name = "mailExecutor")
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(10);
    executor.setKeepAliveSeconds(20);
    executor.setThreadNamePrefix("Async MailExecutor-");
    executor.setRejectedExecutionHandler((r, exec) -> {
      throw new IllegalArgumentException("더 이상 비동기 요청을 처리할 수 없습니다.");
    });
    executor.initialize();
    return executor;
  }

  @Bean(name = "imageUploadExecutor")
  public Executor imageUploadExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setThreadGroupName("imageUploadExecutor");
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(10);
    executor.setKeepAliveSeconds(20);
    executor.setThreadNamePrefix("Async ImageUploadExecutor-");
    executor.setRejectedExecutionHandler((r, exec) -> {
      throw new IllegalArgumentException("더 이상 비동기 요청을 처리할 수 없습니다.");
    });
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return AsyncConfigurer.super.getAsyncUncaughtExceptionHandler();
  }
}
