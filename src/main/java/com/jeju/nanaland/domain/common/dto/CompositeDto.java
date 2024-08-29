package com.jeju.nanaland.domain.common.dto;

import com.jeju.nanaland.domain.common.data.Language;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompositeDto {

  private Long id;
  private String contact;
  private String locale;
  private String title;
  private String content;
  private String address;
  private String addressTag;
  private String time;
  private ImageFileDto firstImage;

  @QueryProjection
  public CompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      Language locale, String title, String content, String address, String addressTag,
      String time) {
    this.id = id;
    this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    this.contact = contact;
    this.locale = locale.toString();
    this.title = title;
    this.content = content;
    this.address = address;
    this.addressTag = addressTag;
    this.time = time;
  }
}
