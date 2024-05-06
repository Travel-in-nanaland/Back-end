package com.jeju.nanaland.domain.festival.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.FESTIVAL;

import com.jeju.nanaland.domain.common.entity.DayOfWeek;
import com.jeju.nanaland.domain.common.entity.Locale;
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
import com.jeju.nanaland.global.exception.ServerErrorException;
import jakarta.transaction.Transactional;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalService {

  private final FestivalRepository festivalRepository;
  private final FavoriteService favoriteService;
  private final SearchService searchService;

  public FestivalThumbnailDto getPastFestivalList(MemberInfoDto memberInfoDto, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    // compositeDto로 종료된 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByOnGoing(
        memberInfoDto.getLanguage().getLocale(), pageable, false);

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
        memberInfoDto.getLanguage().getLocale(), pageable, startDate, endDate, addressFilterList);

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
        memberInfoDto.getLanguage().getLocale(), pageable, seasonKoreanValue);

    List<Long> favoriteIds = getMemberFavoriteFestivalIds(memberInfoDto);

    return getFestivalThumbnailDtoByCompositeDto(memberInfoDto, festivalCompositeDtoList,
        favoriteIds);

  }

  public FestivalDetailDto getFestivalDetail(MemberInfoDto memberInfoDto, Long id,
      boolean isSearch) {
    FestivalCompositeDto compositeDtoById = festivalRepository.findCompositeDtoById(id,
        memberInfoDto.getLanguage().getLocale());

    if (compositeDtoById == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    if (isSearch) {
      searchService.updateSearchVolumeV1(FESTIVAL, id);
    }

    boolean isPostInFavorite = favoriteService.isPostInFavorite(memberInfoDto.getMember(), FESTIVAL,
        id);

    return FestivalDetailDto.builder()
        .id(compositeDtoById.getId())
        .originUrl(compositeDtoById.getOriginUrl())
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
        .build();


  }

  // 매일 00시마다 종료된 축제 상태 업데이트
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void updateOnGoingToFalse() {
    List<Festival> finishedFestival = festivalRepository.findAllByOnGoingAndEndDateBefore(
        true, LocalDate.now());

    if (!finishedFestival.isEmpty()) {
      finishedFestival.forEach(festival -> festival.updateOnGoing(false));
    }
  }

  public FestivalThumbnailDto getFestivalThumbnailDtoByCompositeDto(
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
              .title(dto.getTitle())
              .thumbnailUrl(dto.getThumbnailUrl())
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
  public String formatLocalDateToStringWithDayOfWeek(MemberInfoDto memberInfoDto,
      LocalDate startDate, LocalDate endDate) {
    String nationalDateFormat = memberInfoDto.getLanguage().getDateFormat().replace("-", ". ");
    log.info("1st =>{}", startDate.format(DateTimeFormatter.ofPattern(nationalDateFormat)));
    String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern(nationalDateFormat));
    String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern(nationalDateFormat));

// LocalDate 타입의 startDate, endDate를 04.1(월) ~ 05.13(수)형태로 formatting
    String startDayOfWeek = getDayOfWeekByLocale(memberInfoDto.getLanguage().getLocale(),
        startDate);
    String endDayOfWeek = getDayOfWeekByLocale(memberInfoDto.getLanguage().getLocale(), endDate);
    return formattedStartDate + "(" + startDayOfWeek + ")" + " ~ " + formattedEndDate + "("
        + endDayOfWeek
        + ")";
  }

  // 24.04.01 ~ 24.05.13
  public String formatLocalDateToStringWithoutDayOfWeek(MemberInfoDto memberInfoDto,
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

  public String seasonValueChangeToKorean(String season) {
    return switch (season) {
      case "spring" -> "봄";
      case "summer" -> "여름";
      case "autumn" -> "가을";
      case "winter" -> "겨울";
      default -> throw new BadRequestException("계절 정보 오류");
    };
  }

  public List<Long> getMemberFavoriteFestivalIds(MemberInfoDto memberInfoDto) {
    return favoriteService.getMemberFavoritePostIds(memberInfoDto.getMember(), FESTIVAL);
  }

  //월,화,수,목 형태로 한국어 요일 반환
  public String getKoreanDayOfWeek(LocalDate date) {
    return date.getDayOfWeek()
        .getDisplayName(TextStyle.SHORT, java.util.Locale.KOREA);
  }

  public String getDayOfWeekByLocale(Locale locale, LocalDate date) {
    String result = null;

    if (locale == Locale.KOREAN) { //한국어 요일
      result = getKoreanDayOfWeek(date);
    } else if ((locale == Locale.ENGLISH) || locale == Locale.MALAYSIA) {// 영어와 말레이시아어는 영어로 표기
      result = date.getDayOfWeek()
          .getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH);
    } else if (locale == Locale.VIETNAMESE) { // 베트남어 일 경우
      String koreanDayOfWeek = getKoreanDayOfWeek(date);

      result = switch (koreanDayOfWeek) {
        case "월" -> DayOfWeek.VIE_MON.getValue();
        case "화" -> DayOfWeek.VIE_TUE.getValue();
        case "수" -> DayOfWeek.VIE_WED.getValue();
        case "목" -> DayOfWeek.VIE_THU.getValue();
        case "금" -> DayOfWeek.VIE_FRI.getValue();
        case "토" -> DayOfWeek.VIE_SAT.getValue();
        case "일" -> DayOfWeek.VIE_SUN.getValue();
        default -> result;
      };

    } else if (locale == Locale.CHINESE) {
      String koreanDayOfWeek = getKoreanDayOfWeek(date);

      result = switch (koreanDayOfWeek) {
        case "월" -> DayOfWeek.CN_MON.getValue();
        case "화" -> DayOfWeek.CN_TUE.getValue();
        case "수" -> DayOfWeek.CN_WED.getValue();
        case "목" -> DayOfWeek.CN_THU.getValue();
        case "금" -> DayOfWeek.CN_FRI.getValue();
        case "토" -> DayOfWeek.CN_SAT.getValue();
        case "일" -> DayOfWeek.CN_SUN.getValue();
        default -> result;
      };

    }
    if (result == null) {
      throw new ServerErrorException(ErrorCode.DAY_OF_WEEK_MAPPING_ERROR.getMessage());
    }
    return result;

  }
}
