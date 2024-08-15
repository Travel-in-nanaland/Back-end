package com.jeju.nanaland.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(5);
    executor.setKeepAliveSeconds(20);
    executor.setThreadNamePrefix("async-executor-");
    executor.setRejectedExecutionHandler((r, exec) -> {
      throw new IllegalArgumentException("더 이상 비동기 요청을 처리할 수 없습니다.");
    });
    executor.initialize();
    return executor;
  }
}
