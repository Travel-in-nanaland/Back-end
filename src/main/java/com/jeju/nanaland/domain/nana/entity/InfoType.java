package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.Locale;
import lombok.Getter;

@Getter
public enum InfoType {
  ADDRESS("주소", "enAd", "viAd", "msAd", "zhAd"),
  PARKING("주차", "enPa", "viPa", "msPa", "zhPa"),
  SPECIAL("차별점", "enSp", "viSp", "msSp", "zhSp"),
  AMENITY("편의시설", "enAm", "viAm", "msAm", "zhAm"),
  WEBSITE("홈페이지", "enWb", "viWb", "msWb", "zhWb"),
  RESERVATION_LINK("예약링크", "enRL", "viRL", "msRL", "zhRL"),
  AGE("이용연령", "enAg", "viAg", "msAg", "zhAg"),
  TIME("이용시간", "enTi", "viTi", "msTi", "zhTi"),
  FEE("이용요금", "enFee", "viFee", "msFee", "zhFee"),
  DATE("이용날짜", "enDate", "viDate", "msDate", "zhDate"),
  DESCRIPTION("소개", "enDesc", "viDesc", "msDesc", "zhDesc");


  private final String kr;
  private final String en; //영어
  private final String vi; //베트남어
  private final String ms; //말레이시아어
  private final String zh; //중국어

  InfoType(String kr, String en, String vi, String ms, String zh) {
    this.kr = kr;
    this.en = en;
    this.vi = vi;
    this.ms = ms;
    this.zh = zh;
  }

  public static InfoType contains(String name) {
    for (InfoType infoType : values()) {
      if (infoType.name().equals(name)) {
        return infoType;
      }
    }
  }

  public String getValueByLocale(Locale locale) {
    return switch (locale) {
      case KOREAN -> this.getKr();
      case ENGLISH -> this.getEn();
      case CHINESE -> this.getZh();
      case MALAYSIA -> this.getMs();
      case VIETNAMESE -> this.getVi();
    };
  }
}
