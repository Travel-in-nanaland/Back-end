package com.jeju.nanaland.global.image_upload.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3VideoDto {

  private String originUrl;
}
