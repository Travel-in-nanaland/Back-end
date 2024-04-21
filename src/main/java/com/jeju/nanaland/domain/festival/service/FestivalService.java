package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnail;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.member.entity.Member;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalService {

  private final FestivalRepository festivalRepository;
  private final FavoriteService favoriteService;

  @Transactional
  public String toggleLikeStatus(Member member, Long postId) {
    festivalRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 축제 게시물이 존재하지 않습니다."));

    return favoriteService.toggleLikeStatus(member, CategoryContent.FESTIVAL, postId);
  }

  public FestivalThumbnailDto getPastFestivalList(Locale locale, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    // compositeDto로 종료된 festival 가져오기
    Page<FestivalCompositeDto> festivalCompositeDtoList = festivalRepository.searchCompositeDtoByOnGoing(
        locale, pageable, false);

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

    String startDay = startDate.getDayOfWeek()
        .getDisplayName(TextStyle.SHORT, java.util.Locale.KOREA);
    String endDay = endDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, java.util.Locale.KOREA);
    return formattedStartDate + "(" + startDay + ")" + " ~ " + formattedEndDate + "(" + endDay
        + ")";
  }
}
