package com.jeju.nanaland.domain.festival.service;

import static com.jeju.nanaland.domain.common.data.Category.FESTIVAL;


import com.jeju.nanaland.domain.common.data.DayOfWeek;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalDetailDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnail;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalService {

  private final FestivalRepository festivalRepository;
  private final FavoriteService favoriteService;
  private final SearchService searchService;
  private final ImageFileRepository imageFileRepository;

  public FestivalThumbnailDto getPastFestivalList(MemberInfoDto memberInfoDto, int page, int size,
      List<String> addressFilterList) {
    Pageable pageable = PageRequest.of(page, size);

    // compositeDto로 종료된 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByOnGoing(
        memberInfoDto.getLanguage(), pageable, false, addressFilterList);

    List<Long> favoriteIds = getMemberFavoriteFestivalIds(memberInfoDto);

    return getFestivalThumbnailDtoByCompositeDto(memberInfoDto, festivalCompositeDtoList,
        favoriteIds);
  }

  public FestivalThumbnailDto getThisMonthFestivalList(MemberInfoDto memberInfoDto, int page,
      int size,
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
        throw new BadRequestException(ErrorCode.START_DATE_AFTER_END_DATE.getMessage());
      }
    }
    // compositeDto로 기간에 맞는 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByMonth(
        memberInfoDto.getLanguage(), pageable, startDate, endDate, addressFilterList);

    List<Long> favoriteIds = getMemberFavoriteFestivalIds(memberInfoDto);

    return getFestivalThumbnailDtoByCompositeDto(memberInfoDto, festivalCompositeDtoList,
        favoriteIds);
  }

  public FestivalThumbnailDto getSeasonFestivalList(MemberInfoDto memberInfoDto, int page, int size,
      String season) {
    Pageable pageable = PageRequest.of(page, size);

    // 없는 계절이면(계절 요청 오류)
    String seasonKoreanValue = seasonValueChangeToKorean(season);

    // compositeDto로 계절별 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoBySeason(
        memberInfoDto.getLanguage(), pageable, seasonKoreanValue);

    List<Long> favoriteIds = getMemberFavoriteFestivalIds(memberInfoDto);

    return getFestivalThumbnailDtoByCompositeDto(memberInfoDto, festivalCompositeDtoList,
        favoriteIds);

  }

  public FestivalDetailDto getFestivalDetail(MemberInfoDto memberInfoDto, Long id,
      boolean isSearch) {
    FestivalCompositeDto compositeDtoById = festivalRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage());

    if (compositeDtoById == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    if (isSearch) {
      searchService.updateSearchVolumeV1(FESTIVAL, id);
    }

    boolean isPostInFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), FESTIVAL,
        id);

    // TODO: Image Service로 분리
    List<ImageFileDto> images = new ArrayList<>();
    images.add(compositeDtoById.getFirstImage());
    images.addAll(imageFileRepository.findPostImageFiles(id));

    return FestivalDetailDto.builder()
        .id(compositeDtoById.getId())
        .addressTag(compositeDtoById.getAddressTag())
        .title(compositeDtoById.getTitle())
        .content(compositeDtoById.getContent())
        .address(compositeDtoById.getAddress())
        .contact(compositeDtoById.getContact())
        .time(compositeDtoById.getTime())
        .fee(compositeDtoById.getFee())
        .homepage(compositeDtoById.getHomepage())
        .period(formatLocalDateToStringWithDayOfWeek(memberInfoDto,
            compositeDtoById.getStartDate(),
            compositeDtoById.getEndDate()))
        .isFavorite(isPostInFavorite)
        .images(images)
        .build();


  }

  // 매일 00시마다 종료된 축제 상태 업데이트
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  protected void updateOnGoingToFalse() {
    List<Festival> finishedFestival = festivalRepository.findAllByOnGoingAndEndDateBefore(
        true, LocalDate.now());

    if (!finishedFestival.isEmpty()) {
      finishedFestival.forEach(festival -> festival.updateOnGoing(false));
    }
  }

  @Transactional
  @Scheduled(cron = "0 0 0 1 1 *") // 매년 1월1일
  protected void updateActiveToInActive() {
    List<Festival> finishedFestival = festivalRepository.findAllByEndDateBefore(
        LocalDate.now().minusYears(2));

    if (!finishedFestival.isEmpty()) {
      finishedFestival.forEach(festival -> festival.updateStatus(Status.INACTIVE));
    }
  }

  private FestivalThumbnailDto getFestivalThumbnailDtoByCompositeDto(
      MemberInfoDto memberInfoDto, Page<FestivalCompositeDto> festivalCompositeDtoList,
      List<Long> favoriteIds) {
    List<FestivalThumbnail> thumbnails = new ArrayList<>();
    for (FestivalCompositeDto dto : festivalCompositeDtoList) {

      // LocalDate 타입의 startDate, endDate를 24. 04. 01 ~ 24. 05. 13형태로 formatting
      String period = formatLocalDateToStringWithoutDayOfWeek(memberInfoDto, dto.getStartDate(),
          dto.getEndDate());

      thumbnails.add(
          FestivalThumbnail.builder()
              .id(dto.getId())
              .imageFileDto(dto.getFirstImage())
              .title(dto.getTitle())
              .period(period)
              .addressTag(dto.getAddressTag())
              .isFavorite(favoriteIds.contains(dto.getId()))
              .build()
      );
    }
    return FestivalThumbnailDto.builder()
        .totalElements(festivalCompositeDtoList.getTotalElements())
        .data(thumbnails)
        .build();
  }

  // 2024.04.01(월) ~ 2024.05.13(화)
  private String formatLocalDateToStringWithDayOfWeek(MemberInfoDto memberInfoDto,
      LocalDate startDate, LocalDate endDate) {
    String nationalDateFormat = memberInfoDto.getLanguage().getDateFormat().replace("-", ". ");

    String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern(nationalDateFormat));
    String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern(nationalDateFormat));

// LocalDate 타입의 startDate, endDate를 04.1(월) ~ 05.13(수)형태로 formatting
    String startDayOfWeek = getDayOfWeekByLocale(memberInfoDto.getLanguage(),
        startDate);
    String endDayOfWeek = getDayOfWeekByLocale(memberInfoDto.getLanguage(), endDate);
    return formattedStartDate + "(" + startDayOfWeek + ")" + " ~ " + formattedEndDate + "("
        + endDayOfWeek
        + ")";
  }

  // 24.04.01 ~ 24.05.13
  private String formatLocalDateToStringWithoutDayOfWeek(MemberInfoDto memberInfoDto,
      LocalDate startDate, LocalDate endDate) {

    // - 을 . 으로 대체
    String nationalDateFormat = memberInfoDto.getLanguage().getDateFormat().replace("-", ". ");

    // yyyy 포맷을 yy로 변경
    String finalDateFormat = nationalDateFormat.replace("yyyy", "yy");

    String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern(finalDateFormat));
    String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern(finalDateFormat));

// LocalDate 타입의 startDate, endDate를 24.04.1 ~ 24.05.13 형태로 formatting
    return formattedStartDate + " ~ " + formattedEndDate;
  }

  private String seasonValueChangeToKorean(String season) {
    return switch (season) {
      case "spring" -> "봄";
      case "summer" -> "여름";
      case "autumn" -> "가을";
      case "winter" -> "겨울";
      default -> throw new BadRequestException("계절 정보 오류");
    };
  }

  private List<Long> getMemberFavoriteFestivalIds(MemberInfoDto memberInfoDto) {
    return favoriteService.getFavoritePostIdsWithMember(memberInfoDto.getMember());
  }

  private DayOfWeek getIntDayOfWeek(LocalDate date) {
    /*
      getDayOfWeek().getValue()는 월요일이 1부터 시작,
      선언한 enum DayOfWeek은 getValue()로 불러왔을 때 0부터시작이므로 -1 작성
     */
    return DayOfWeek.values()[date.getDayOfWeek().getValue() - 1];
  }

  public String getDayOfWeekByLocale(Language locale, LocalDate date) {
    return getIntDayOfWeek(date).getValueByLocale(locale);
  }
}
