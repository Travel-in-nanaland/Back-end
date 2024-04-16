package com.jeju.nanaland.domain.search.service;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.dto.SearchResponse.ThumbnailDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

  private final NatureRepository natureRepository;
  private final ExperienceRepository experienceRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;
  private final RedisTemplate<String, String> redisTemplate;

  public SearchResponse.CategoryDto getCategorySearchResultDto(String keyword, Locale locale) {
    // Redis에 해당 검색어 count + 1
    updateSearchCount(keyword, locale);

    // offset: 0, pageSize: 2
    int page = 0;
    int size = 2;
    return SearchResponse.CategoryDto.builder()
        .nature(getNatureSearchResultDto(keyword, locale, page, size))
        .festival(getFestivalSearchResultDto(keyword, locale, page, size))
        .market(getMarketSearchResultDto(keyword, locale, page, size))
        .experience(getExperienceSearchResultDto(keyword, locale, page, size))
        .build();
  }

  public SearchResponse.ResultDto getNatureSearchResultDto(String keyword, Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureCompositeDto> ResultDto = natureRepository.searchCompositeDtoByTitle(keyword, locale,
        pageable);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (NatureCompositeDto dto : ResultDto) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .count(ResultDto.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public SearchResponse.ResultDto getFestivalSearchResultDto(String keyword, Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<FestivalCompositeDto> ResultDto = festivalRepository.searchCompositeDtoByTitle(keyword,
        locale, pageable);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (FestivalCompositeDto dto : ResultDto) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .count(ResultDto.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public SearchResponse.ResultDto getExperienceSearchResultDto(String keyword, Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<ExperienceCompositeDto> ResultDto = experienceRepository.searchCompositeDtoByTitle(keyword,
        locale, pageable);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (ExperienceCompositeDto dto : ResultDto) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .count(ResultDto.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public SearchResponse.ResultDto getMarketSearchResultDto(String keyword, Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<MarketCompositeDto> ResultDto = marketRepository.searchCompositeDtoByTitle(keyword, locale,
        pageable);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (MarketCompositeDto dto : ResultDto) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .count(ResultDto.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public List<String> getPopularSearch(Locale locale) {
    String language = locale.name();

    LocalDateTime current = LocalDateTime.now();
    String keyIdx = String.valueOf(current.getHour() % 3);
    String key = "ranking_" + language + "_" + keyIdx;
    log.info("keyIdx : {}", keyIdx);

    // 가장 검색어가 많은 8개
    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
    Set<TypedTuple<String>> typedTuples = zSetOperations.reverseRangeByScoreWithScores(key, 0, 7);
    if (typedTuples != null) {
      List<String> rankList = new ArrayList<>();
      for (TypedTuple<String> typedTuple : typedTuples) {
        rankList.add(typedTuple.getValue());
      }

      return rankList;
    }

    return null;
  }

  private void updateSearchCount(String title, Locale locale) {
    String language = locale.name();
    String key0 = "ranking_" + language + "_0";
    String key1 = "ranking_" + language + "_1";
    String key2 = "ranking_" + language + "_2";

    LocalDateTime current = LocalDateTime.now();
    int currentHour = current.getHour();
    switch (currentHour % 3) {
      case 0 -> {
        // 0번, 1번 key에 갱신
        redisTemplate.opsForZSet().incrementScore(key0, title, 1);
        redisTemplate.opsForZSet().incrementScore(key1, title, 1);

        // 3번 key 삭제
        redisTemplate.delete(key2);
      }
      case 1 -> {
        // 1번, 2번 key에 갱신
        redisTemplate.opsForZSet().incrementScore(key1, title, 1);
        redisTemplate.opsForZSet().incrementScore(key2, title, 1);

        // 3번 key 삭제
        redisTemplate.delete(key0);
      }
      case 2 -> {
        // 2번, 0번 key에 갱신
        redisTemplate.opsForZSet().incrementScore(key2, title, 1);
        redisTemplate.opsForZSet().incrementScore(key0, title, 1);

        // 1번 key 삭제
        redisTemplate.delete(key1);
      }
    }
  }
}
