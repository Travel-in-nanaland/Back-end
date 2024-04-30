package com.jeju.nanaland.domain.common.dto;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompositeDto {

  private Long id;
  private String originUrl;
  private String thumbnailUrl;
  private String contact;
  private String locale;
  private String title;
  private String content;
  private String address;
  private String addressTag;
  private String time;

  @QueryProjection
  public CompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      Locale locale, String title, String content, String address,
      String addressTag, String time) {
    this.id = id;
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.contact = contact;
    this.locale = locale.toString();
    this.title = title;
    this.content = content;
    this.address = address;
    this.addressTag = addressTag;
    this.time = time;
  }
}
