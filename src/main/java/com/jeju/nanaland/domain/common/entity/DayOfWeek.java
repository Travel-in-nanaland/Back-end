package com.jeju.nanaland.domain.common.entity;

public enum DayOfWeek {

  // 한국 요일
  KOREAN_MON("월"), KOREAN_TUE("화"), KOREAN_WED("수"), KOREAN_THU("목"),
  KOREAN_FRI("금"), KOREAN_SAT("토"), KOREAN_SUN("일"),

  // 영어, 말레이시아
  ENG_MS_MON("MON"), ENG_MS_TUE("TUE"), ENG_MS_WED("WED"), ENG_MS_THU("THU"),
  ENG_MS_FRI("FRI"), ENG_MS_SAT("SAT"), ENG_MS_SUN("SUN"),

  //베트남어
  VIE_MON("T2"), VIE_TUE("T3"), VIE_WED("T4"), VIE_THU("T5"),
  VIE_FRI("T6"), VIE_SAT("T7"), VIE_SUN("CN"),

  //중국어
  CN_MON("周一"), CN_TUE("周二"), CN_WED("周三"), CN_THU("周四"),
  CN_FRI("周五"), CN_SAT("周六"), CN_SUN("周日");
  private final String value;

  DayOfWeek(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

