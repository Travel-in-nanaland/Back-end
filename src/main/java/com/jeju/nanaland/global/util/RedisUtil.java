package com.jeju.nanaland.global.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final RedisTemplate<String, String> redisTemplate;

  public String getValue(String key) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    return valueOperations.get(key);
  }

  public String getRecentDataFromList(String key) {
    ListOperations<String, String> listOperations = redisTemplate.opsForList();
    return listOperations.index(key, 0);
  }

  public void trimAndPushLeft(String key, long count, String value) {
    ListOperations<String, String> listOperations = redisTemplate.opsForList();
    listOperations.trim(key, 0, count - 2);
    listOperations.leftPush(key, value);
  }

  public void deleteData(String key) {
    redisTemplate.delete(key);
  }

  public void setExpiringValue(String key, String value, long duration) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    Duration expireDuration = Duration.ofSeconds(duration);
    valueOperations.set(key, value, expireDuration);
  }
}
