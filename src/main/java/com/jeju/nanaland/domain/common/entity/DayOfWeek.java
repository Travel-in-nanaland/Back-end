package com.jeju.nanaland.domain.common.entity;

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

  private final String korean;
  private final String english; //영어
  private final String vietnamese; //베트남어
  private final String malaysia; //말레이시아어
  private final String chinese; //중국어

  DayOfWeek(String korean, String english, String vietnamese, String malaysia, String chinese) {
    this.korean = korean;
    this.english = english;
    this.vietnamese = vietnamese;
    this.malaysia = malaysia;
    this.chinese = chinese;
  }
}

