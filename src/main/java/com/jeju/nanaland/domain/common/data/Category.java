package com.jeju.nanaland.domain.common.data;

import lombok.Getter;

@Getter
public enum Category {
  NANA("나나's pick", "enNana", "viNana", "msNana", "zaNana"),
  NANA_CONTENT("나나 상세", "enNanaContent", "vienNanaContent", "msenNanaContent", "zhenNanaContent"),
  EXPERIENCE("이색 체험", "enExperience", "viExperience", "msExperience", "zhExperience"),
  FESTIVAL("축제", "enFestival", "viFestival", "msFestival", "zhFestival"),
  NATURE("7대 자연", "enNature", "viNature", "msNature", "zhNature"),
  MARKET("전통시장", "enMarket", "viMarket", "msMarket", "zhMarket"),
  RESTAURANT("제주 맛집", "enRestaurant", "viRestaurant", "msRestaurant", "zhRestaurant");

  private final String kr;
  private final String en; //영어
  private final String vi; //베트남어
  private final String ms; //말레이시아어
  private final String zh; //중국어

  Category(String kr, String en, String vi, String ms, String zh) {
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
