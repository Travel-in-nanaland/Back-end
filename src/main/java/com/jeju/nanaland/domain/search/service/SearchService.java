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

  public SearchResponse.CategoryDto getCategorySearchResultDto(String title, Locale locale) {
    // Redis에 해당 검색어 count + 1
    updateSearchCount(title, locale);

    // offset: 0, pageSize: 2
    Pageable pageable = PageRequest.of(0, 2);

    return SearchResponse.CategoryDto.builder()
        .nature(getNatureSearchResultDto(title, locale, pageable))
        .festival(getFestivalSearchResultDto(title, locale, pageable))
        .market(getMarketSearchResultDto(title, locale, pageable))
        .experience(getExperienceSearchResultDto(title, locale, pageable))
        .build();
  }

  public SearchResponse.ResultDto getNatureSearchResultDto(String title, Locale locale,
      Pageable pageable) {

    Page<NatureCompositeDto> ResultDto = natureRepository.searchCompositeDtoByTitle(title, locale,
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

  public SearchResponse.ResultDto getFestivalSearchResultDto(String title, Locale locale,
      Pageable pageable) {

    Page<FestivalCompositeDto> ResultDto = festivalRepository.searchCompositeDtoByTitle(title,
        locale,
        pageable);

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

  public SearchResponse.ResultDto getExperienceSearchResultDto(String title, Locale locale,
      Pageable pageable) {

    Page<ExperienceCompositeDto> ResultDto = experienceRepository.searchCompositeDtoByTitle(title,
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

  public SearchResponse.ResultDto getMarketSearchResultDto(String title, Locale locale,
      Pageable pageable) {

    Page<MarketCompositeDto> ResultDto = marketRepository.searchCompositeDtoByTitle(title, locale,
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
    String key = "ranking_" + language;

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
    /**
     * TODO: 시간별로 key를 구성하고 업데이트
     */
    String language = locale.name();
    String key = "ranking_" + language;

    redisTemplate.opsForZSet().incrementScore(key, title, 1);
  }
}
