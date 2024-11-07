package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.repository.PostRepository;
import com.jeju.nanaland.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostViewCountService {

  private final PostRepository postRepository;
  private final RedisUtil redisUtil;

  @Transactional
  public void increaseViewCount(Long postId, Long memberId) {
    String redisKey = "post_viewed_" + memberId + "_" + postId;

    // 30분 -> 1800초
    long cacheDurationSeconds = 1800L;

    // 30분 이내에 조회한 기록이 없으면
    if (redisUtil.getValue(redisKey) == null) {
      // 조회수 증가
      postRepository.increaseViewCount(postId);
      // 레디스에 등록
      redisUtil.setExpiringValue(redisKey, "viewed", cacheDurationSeconds);
    }

  }
}