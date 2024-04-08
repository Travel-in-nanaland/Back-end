package com.jeju.nanaland.domain.search.service;

import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.search.dto.SearchResponseDto;
import com.jeju.nanaland.domain.search.dto.SearchResponseDto.Thumbnail;
import com.jeju.nanaland.domain.stay.dto.StayCompositeDto;
import com.jeju.nanaland.domain.stay.repository.StayRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

  public SearchResponseDto.Category getCategorySearchResult(String title, String locale,
      Pageable pageable) {

    return SearchResponseDto.Category.builder()
        .stay(getStaySearchResult(title, locale, pageable))
        .nature(getNatureSearchResult(title, locale, pageable))
        .festival(getFestivalSearchResult(title, locale, pageable))
        .market(getMarketSearchResult(title, locale, pageable))
        .experience(getExperienceSearchResult(title, locale, pageable))
        .build();
  }

  private SearchResponseDto.Result getStaySearchResult(String title, String locale,
      Pageable pageable) {

    Page<StayCompositeDto> result = stayRepository.searchCompositeDtoByTitle(title, locale,
        pageable);

    List<SearchResponseDto.Thumbnail> thumbnails = new ArrayList<>();
    for (StayCompositeDto dto : result) {
      thumbnails.add(
          Thumbnail.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponseDto.Result.builder()
        .count(result.getTotalElements())
        .data(thumbnails)
        .build();
  }

  private SearchResponseDto.Result getNatureSearchResult(String title, String locale,
      Pageable pageable) {

    Page<NatureCompositeDto> result = natureRepository.searchCompositeDtoByTitle(title, locale,
        pageable);

    List<SearchResponseDto.Thumbnail> thumbnails = new ArrayList<>();
    for (NatureCompositeDto dto : result) {
      thumbnails.add(
          Thumbnail.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponseDto.Result.builder()
        .count(result.getTotalElements())
        .data(thumbnails)
        .build();
  }

  private SearchResponseDto.Result getFestivalSearchResult(String title, String locale,
      Pageable pageable) {

    Page<FestivalCompositeDto> result = festivalRepository.searchCompositeDtoByTitle(title, locale,
        pageable);

    List<SearchResponseDto.Thumbnail> thumbnails = new ArrayList<>();
    for (FestivalCompositeDto dto : result) {
      thumbnails.add(
          Thumbnail.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponseDto.Result.builder()
        .count(result.getTotalElements())
        .data(thumbnails)
        .build();
  }

  private SearchResponseDto.Result getExperienceSearchResult(String title, String locale,
      Pageable pageable) {

    Page<ExperienceCompositeDto> result = experienceRepository.searchCompositeDtoByTitle(title,
        locale, pageable);

    List<SearchResponseDto.Thumbnail> thumbnails = new ArrayList<>();
    for (ExperienceCompositeDto dto : result) {
      thumbnails.add(
          Thumbnail.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponseDto.Result.builder()
        .count(result.getTotalElements())
        .data(thumbnails)
        .build();
  }

  private SearchResponseDto.Result getMarketSearchResult(String title, String locale,
      Pageable pageable) {

    Page<MarketCompositeDto> result = marketRepository.searchCompositeDtoByTitle(title, locale,
        pageable);

    List<SearchResponseDto.Thumbnail> thumbnails = new ArrayList<>();
    for (MarketCompositeDto dto : result) {
      thumbnails.add(
          Thumbnail.builder()
              .id(dto.getId())
              .thumbnailUrl(dto.getThumbnailUrl())
              .title(dto.getTitle())
              .build());
    }

    return SearchResponseDto.Result.builder()
        .count(result.getTotalElements())
        .data(thumbnails)
        .build();
  }
}
