package com.jeju.nanaland.domain.search.service;

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
import com.jeju.nanaland.domain.stay.dto.StayCompositeDto;
import com.jeju.nanaland.domain.stay.repository.StayRepository;
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

  private final StayRepository stayRepository;
  private final NatureRepository natureRepository;
  private final ExperienceRepository experienceRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;

  public SearchResponse.CategoryDto getCategorySearchResultDto(String title, String locale) {

    // offset: 0, pageSize: 2
    Pageable pageable = PageRequest.of(0, 2);

    return SearchResponse.CategoryDto.builder()
        .stay(getStaySearchResultDto(title, locale, pageable))
        .nature(getNatureSearchResultDto(title, locale, pageable))
        .festival(getFestivalSearchResultDto(title, locale, pageable))
        .market(getMarketSearchResultDto(title, locale, pageable))
        .experience(getExperienceSearchResultDto(title, locale, pageable))
        .build();
  }

  public SearchResponse.ResultDto getStaySearchResultDto(String title, String locale,
      Pageable pageable) {

    Page<StayCompositeDto> ResultDto = stayRepository.searchCompositeDtoByTitle(title, locale,
        pageable);

    List<SearchResponse.ThumbnailDto> thumbnails = new ArrayList<>();
    for (StayCompositeDto dto : ResultDto) {
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

  public SearchResponse.ResultDto getNatureSearchResultDto(String title, String locale,
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

  public SearchResponse.ResultDto getFestivalSearchResultDto(String title, String locale,
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

  public SearchResponse.ResultDto getExperienceSearchResultDto(String title, String locale,
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

  public SearchResponse.ResultDto getMarketSearchResultDto(String title, String locale,
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
}
