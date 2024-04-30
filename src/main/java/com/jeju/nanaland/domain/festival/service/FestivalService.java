package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnail;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
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
public class FestivalService {

  private final FestivalRepository festivalRepository;
  private final FavoriteService favoriteService;

  public FestivalThumbnailDto getPastFestivalList(Locale locale, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    // compositeDto로 종료된 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByOnGoing(
        locale, pageable, false);

    return getFestivalThumbnailDtoByCompositeDto(festivalCompositeDtoList);
  }

  public FestivalThumbnailDto getThisMonthFestivalList(Locale locale, int page, int size,
      List<String> addressFilterList, LocalDate startDate, LocalDate endDate) {
    Pageable pageable = PageRequest.of(page, size);
    if (startDate == null && endDate == null) {
      // 오늘 날짜 가져오기
      LocalDate now = LocalDate.now();
      startDate = now;
      endDate = now;
    } else {
      assert startDate != null; // null 검사하기
      if (startDate.isAfter(endDate)) {
        throw new BadRequestException("endDate보다 startDate가 더 큽니다.");
      }
    }
    // compositeDto로 기간에 맞는 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByMonth(
        locale, pageable, startDate, endDate, addressFilterList);

    return getFestivalThumbnailDtoByCompositeDto(festivalCompositeDtoList);
  }

  public FestivalThumbnailDto getSeasonFestivalList(Locale locale, int page, int size,
      String season) {
    Pageable pageable = PageRequest.of(page, size);

    // 없는 계절이면(계절 요청 오류)
    String seasonKoreanValue = seasonValueChangeToKorean(season);

    // compositeDto로 계절별 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoBySeason(
        locale, pageable, seasonKoreanValue);
    getFestivalThumbnailDtoByCompositeDto(festivalCompositeDtoList);

    return getFestivalThumbnailDtoByCompositeDto(festivalCompositeDtoList);

  }

  public FestivalThumbnailDto getFestivalThumbnailDtoByCompositeDto(
      Page<FestivalCompositeDto> festivalCompositeDtoList) {
    List<FestivalThumbnail> thumbnails = new ArrayList<>();
    for (FestivalCompositeDto dto : festivalCompositeDtoList) {

      // LocalDate 타입의 startDate, endDate를 04.1(월) ~ 05.13(수)형태로 formatting
      String period = formatLocalDateToStringWithDay(dto.getStartDate(), dto.getEndDate());
      thumbnails.add(
          FestivalThumbnail.builder()
              .id(dto.getId())
              .title(dto.getTitle())
              .thumbnailUrl(dto.getThumbnailUrl())
              .period(period)
              .addressTag(dto.getAddressTag())
              .build()
      );
    }
    return FestivalThumbnailDto.builder()
        .totalElements((long) thumbnails.size())
        .data(thumbnails)
        .build();
  }

  public String formatLocalDateToStringWithDay(LocalDate startDate, LocalDate endDate) {
    String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("M. dd"));
    String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("M. dd"));
// LocalDate 타입의 startDate, endDate를 04.1(월) ~ 05.13(수)형태로 formatting
    String startDay = startDate.getDayOfWeek()
        .getDisplayName(TextStyle.SHORT, java.util.Locale.KOREA);
    String endDay = endDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, java.util.Locale.KOREA);
    return formattedStartDate + "(" + startDay + ")" + " ~ " + formattedEndDate + "(" + endDay
        + ")";
  }

  public String seasonValueChangeToKorean(String season) {
    return switch (season) {
      case "spring" -> "봄";
      case "summer" -> "여름";
      case "autumn" -> "가을";
      case "winter" -> "겨울";
      default -> throw new BadRequestException("계절 정보 오류");
    };
  }
}
