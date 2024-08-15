package com.jeju.nanaland.domain.review.service;

import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewAsyncService {

  /**
   * redis에서 모든 값 가져온 후 key가 keyword를 포함한 것 찾기 찾은 것들에 value->(SearchPostForReviewDto) 가져오기 title로
   * 정렬
   */
  @Async
  public CompletableFuture<List<SearchPostForReviewDto>> searchByKeyword(
      Map<String, SearchPostForReviewDto> redisMap,
      String keyword) {

    List<SearchPostForReviewDto> result = redisMap.entrySet().stream()
        .filter(entry -> entry.getKey().toLowerCase().contains(keyword.toLowerCase()))
        .map(Entry::getValue)
        .sorted(Comparator.comparing(SearchPostForReviewDto::getTitle))
        .collect(Collectors.toList());

    return CompletableFuture.completedFuture(result);
  }

  /**
   * redis에서 모든 값 가져온 후 List<> keyword를 stream 돌려서 key가 keyword를 포함한 것 찾기 찾은 것들에
   * value->(SearchPostForReviewDto) 가져오기 title로 정렬
   */
  @Async
  public CompletableFuture<List<SearchPostForReviewDto>> searchByKeywordList(
      Map<String, SearchPostForReviewDto> redisMap, List<String> keywords) {

    List<SearchPostForReviewDto> result = redisMap.entrySet().stream()
        .filter(entry -> keywords.stream()
            .anyMatch(keyword -> entry.getKey().toLowerCase().contains(keyword.toLowerCase())))
        .map(Entry::getValue)
        .sorted(Comparator.comparing(SearchPostForReviewDto::getTitle))
        .collect(Collectors.toList());

    return CompletableFuture.completedFuture(result);
  }
}
