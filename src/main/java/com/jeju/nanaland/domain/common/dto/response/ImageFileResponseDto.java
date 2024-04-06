package com.jeju.nanaland.domain.common.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ImageFileResponseDto {

  private Long id;
  private String thumbnailUrl;
  private String originUrl;
}
