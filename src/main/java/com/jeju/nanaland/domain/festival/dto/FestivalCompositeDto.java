package com.jeju.nanaland.domain.festival.dto;

import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalCompositeDto extends CompositeDto {

  private String homepage;
  private String intro;
  private String fee;
  private LocalDate startDate;
  private LocalDate endDate;
  private String season;

  @QueryProjection
  public FestivalCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      String homepage, Locale locale, String title, String content, String address,
      String addressTag, String time, String intro, String fee, LocalDate startDate,
      LocalDate endDate, String season) {
    super(id, originUrl, thumbnailUrl, contact, locale, title, content, address, addressTag, time);
    this.homepage = homepage;
    this.intro = intro;
    this.fee = fee;
    this.startDate = startDate;
    this.endDate = endDate;
    this.season = season;
  }
}
