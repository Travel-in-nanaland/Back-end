package com.jeju.nanaland.domain.common.data;

import lombok.Getter;

@Getter
public enum DayOfWeek {

  MONDAY("월", "MON", "MON", "T2", "周一"),
  TUESDAY("화", "TUE", "TUE", "T3", "周二"),
  WEDNESDAY("수", "WED", "WED", "T4", "周三"),
  THURSDAY("목", "THU", "THU", "T5", "周四"),
  FRIDAY("금", "FRI", "FRI", "T6", "周五"),
  SATURDAY("토", "SAT", "SAT", "T7", "周六"),
  SUNDAY("일", "SUN", "SUN", "CN", "周日");

  private final String kr;
  private final String en; //영어
  private final String vi; //베트남어
  private final String ms; //말레이시아어
  private final String zh; //중국어

  DayOfWeek(String kr, String en, String vi, String ms, String zh) {
    this.kr = kr;
    this.en = en;
    this.vi = vi;
    this.ms = ms;
    this.zh = zh;
  }

  public String getValueByLocale(Language locale) {
    return switch (locale) {
      case KOREAN -> this.getKr();
      case ENGLISH -> this.getEn();
      case CHINESE -> this.getZh();
      case MALAYSIA -> this.getMs();
      case VIETNAMESE -> this.getVi();
    };
  }
}

