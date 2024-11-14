package com.jeju.nanaland.global.file.data;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum FileCategory {

  MEMBER_PROFILE(Arrays.asList("jpeg", "jpg", "png", "webp")),
  REVIEW(Arrays.asList("jpeg", "jpg", "png", "webp")),
  INFO_FIX_REPORT(Arrays.asList("jpeg", "jpg", "png", "webp")),
  CLAIM_REPORT(Arrays.asList("jpeg", "jpg", "png", "webp", "mp4", "mov", "webm"));

  private final List<String> allowedExtensions;

  FileCategory(List<String> allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }
}
