package com.jeju.nanaland.global.imageUpload.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3ImageDto {

  private String originUrl;
  private String thumbnailUrl;
}
