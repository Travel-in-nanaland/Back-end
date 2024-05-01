package com.jeju.nanaland.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final RedisTemplate<String, String> redisTemplate;

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
}
