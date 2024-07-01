package com.jeju.nanaland.domain.common.data;

import lombok.Getter;

@Getter
public enum Language {
  KOREAN("yy-MM-dd"),
  ENGLISH("MM-dd-yy"),
  CHINESE("yy-MM-dd"),
  MALAYSIA("dd-MM-yy"),
  VIETNAMESE("dd-MM-yy");

  private final String dateFormat;

  Language(String dateFormat) {
    this.dateFormat = dateFormat;
  }
}
