package com.jeju.nanaland.domain.common.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFileDto {

  private String originUrl;
  private String thumbnailUrl;
}
