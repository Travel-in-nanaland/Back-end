package com.jeju.nanaland.domain.common.entity;

import com.jeju.nanaland.global.exception.NotFoundException;

public enum Locale {
  KOREAN, ENGLISH, CHINESE, MALAYSIA, VIETNAMESE;

  public static Locale contains(String name) {
    for (Locale locale : values()) {
      if (locale.name().equals(name)) {
        return locale;
      }
    }
    throw new NotFoundException("일치하는 Locale이 없습니다.");
  }
}
