package com.jeju.nanaland.domain.festival.entity.enums;

import lombok.Getter;

@Getter
public enum Season {
  SPRING("spring", "봄"),
  SUMMER("summer", "여름"),
  AUTUMN("autumn", "가을"),
  WINTER("winter", "겨울");

  private final String en;
  private final String kr;

  Season(String en, String kr) {
    this.en = en;
    this.kr = kr;
  }

  public String toKorean() {
    return this.getKr();
  }

}
