package com.jeju.nanaland.domain.search.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.CategoryContent.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.CategoryContent.MARKET;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NATURE;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.entity.Member;
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

  private final FavoriteService favoriteService;

  private final RedisTemplate<String, String> redisTemplate;

  public SearchResponse.CategoryDto getCategorySearchResultDto(Member member, String keyword,
      Locale locale) {
    // Redis에 해당 검색어 count + 1
    updateSearchCountV1(keyword, locale);

    // offset: 0, pageSize: 2
    int page = 0;
    int size = 2;
    return SearchResponse.CategoryDto.builder()
        .nature(getNatureSearchResultDto(member, keyword, locale, page, size))
        .festival(getFestivalSearchResultDto(member, keyword, locale, page, size))
        .market(getMarketSearchResultDto(member, keyword, locale, page, size))
        .experience(getExperienceSearchResultDto(member, keyword, locale, page, size))
        .build();
  }

  public SearchResponse.ResultDto getNatureSearchResultDto(Member member, String keyword,
      Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NatureCompositeDto> resultPage = natureRepository.searchCompositeDtoByTitle(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getMemberFavoritePostIds(member, NATURE);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (NatureCompositeDto dto : resultPage) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public SearchResponse.ResultDto getFestivalSearchResultDto(Member member, String keyword,
      Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<FestivalCompositeDto> resultPage = festivalRepository.searchCompositeDtoByTitle(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getMemberFavoritePostIds(member, FESTIVAL);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (FestivalCompositeDto dto : resultPage) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public SearchResponse.ResultDto getExperienceSearchResultDto(Member member, String keyword,
      Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<ExperienceCompositeDto> resultPage = experienceRepository.searchCompositeDtoByTitle(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getMemberFavoritePostIds(member, EXPERIENCE);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (ExperienceCompositeDto dto : resultPage) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public SearchResponse.ResultDto getMarketSearchResultDto(Member member, String keyword,
      Locale locale,
      int page, int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<MarketCompositeDto> resultPage = marketRepository.searchCompositeDtoByTitle(
        keyword, locale, pageable);

    List<Long> favoriteIds = favoriteService.getMemberFavoritePostIds(member, MARKET);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (MarketCompositeDto dto : resultPage) {
      thumbnails.add(
          ThumbnailDto.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build());
    }

    return SearchResponse.ResultDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(thumbnails)
        .build();
  }

  public List<String> getPopularSearch(Locale locale) {
    String language = locale.name();

    // version 1
    String key = "ranking_" + language;

    /*
    // version 2
    LocalDateTime current = LocalDateTime.now();
    String keyIdx = String.valueOf(current.getHour() % 3);
    String key = "ranking_" + language + "_" + keyIdx;
    log.info("keyIdx : {}", keyIdx);
    */

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

  // version1 : 인기검색어 정보 삭제하지 않고 계속 누적됨
  private void updateSearchCountV1(String title, Locale locale) {
    String language = locale.name();
    String key = "ranking_" + language;

    redisTemplate.opsForZSet().incrementScore(key, title, 1);
  }

  // version2 : 인기검색어 정보 1시간마다 갱신됨
  private void updateSearchCountV2(String title, Locale locale) {
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
