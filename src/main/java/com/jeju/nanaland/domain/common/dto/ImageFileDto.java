package com.jeju.nanaland.domain.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFileDto {

  private String originUrl;
  private String thumbnailUrl;

  @QueryProjection
  public ImageFileDto(String originUrl, String thumbnailUrl) {
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
  }
}
