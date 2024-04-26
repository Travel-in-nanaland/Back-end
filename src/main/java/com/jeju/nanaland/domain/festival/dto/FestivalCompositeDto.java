package com.jeju.nanaland.domain.festival.dto;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalCompositeDto {

  private Long id;
  private String originUrl;
  private String thumbnailUrl;
  private String contact;
  private String homepage;
  private String locale;
  private String title;
  private String content;
  private String address;
  private String addressTag;
  private String time;
  private String intro;
  private String fee;
  private LocalDate startDate;
  private LocalDate endDate;

  @QueryProjection
  public FestivalCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      String homepage, Locale locale, String title, String content, String address, String time,
      String addressTag, String intro, String fee, LocalDate startDate, LocalDate endDate) {
    this.id = id;
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.contact = contact;
    this.homepage = homepage;
    this.locale = locale.toString();
    this.title = title;
    this.content = content;
    this.address = address;
    this.addressTag = addressTag;
    this.time = time;
    this.intro = intro;
    this.fee = fee;
    this.startDate = startDate;
    this.endDate = endDate;
  }
}
