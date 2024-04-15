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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

  private final NatureRepository natureRepository;
  private final ExperienceRepository experienceRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;

  public SearchResponse.CategoryDto getCategorySearchResultDto(String keyword, Locale locale) {

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
}
