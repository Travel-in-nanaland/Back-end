package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnail;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
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
      List<String> addressFilterList, int startDate, int endDate) {
    Pageable pageable = PageRequest.of(page, size);

    // 현재 연도와 월을 가져오기
    YearMonth currentYearMonth = YearMonth.now();
    int currentYear = currentYearMonth.getYear();
    int currentMonth = currentYearMonth.getMonthValue();
    if (startDate == 0 && endDate == 0) { // 날짜 필터가 없다면 이번 달 전체 검색
      startDate = 1;

      // 이번 년도 이번 달의 마지막 일 구하는 함수
      endDate = currentYearMonth.lengthOfMonth();
    }
    // compositeDto로 기간에 맞는 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByMonth(
        locale, pageable, LocalDate.of(currentYear, currentMonth, startDate),
        LocalDate.of(currentYear, currentMonth, endDate), addressFilterList);

    return getFestivalThumbnailDtoByCompositeDto(festivalCompositeDtoList);
  }

  public FestivalThumbnailDto getSeasonFestivalList(Locale locale, int page, int size,
      String season) {
    Pageable pageable = PageRequest.of(page, size);
    LocalDate startDate = null;
    LocalDate endDate = null;
    int currentYear = Year.now().getValue();

    switch (season) {
      case "spring":
        startDate = LocalDate.of(currentYear, 3, 1);
        endDate = LocalDate.of(currentYear, 4, 30);
        break;
      case "summer":
        startDate = LocalDate.of(currentYear, 5, 1);
        endDate = LocalDate.of(currentYear, 8, 31);
        break;
      case "autumn":
        startDate = LocalDate.of(currentYear, 9, 1);
        endDate = LocalDate.of(currentYear, 10, 31);
        break;
      case "winter":
        startDate = LocalDate.of(currentYear, 11, 1);
        endDate = LocalDate.of(currentYear, 2, 1);
        break;
    }
    if (startDate == null) {
      throw new BadRequestException("계절 정보 오류");
    }

    // compositeDto로 계절별 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoBySeason(
        locale, pageable, startDate, endDate, currentYear);
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
}
