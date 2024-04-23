package com.jeju.nanaland.domain.nature.dto;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NatureCompositeDto {

  private Long id;
  private String originUrl;
  private String thumbnailUrl;
  private String contact;
  private String locale;
  private String title;
  private String content;
  private String address;
  private String addressTag;
  private String intro;
  private String details;
  private String time;
  private String amenity;

  @QueryProjection
  public NatureCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      Locale locale, String title, String content, String address, String addressTag, String intro,
      String details, String time, String amenity) {
    this.id = id;
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.contact = contact;
    this.locale = locale.toString();
    this.title = title;
    this.content = content;
    this.address = address;
    this.addressTag = addressTag;
    this.intro = intro;
    this.details = details;
    this.time = time;
    this.amenity = amenity;
  }
}
