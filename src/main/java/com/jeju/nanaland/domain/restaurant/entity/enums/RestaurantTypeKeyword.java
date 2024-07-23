package com.jeju.nanaland.domain.restaurant.entity.enums;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum RestaurantTypeKeyword {

  KOREAN("한식", "enAn", "zhAn", "msAn", "viAn"),
  CHINESE("중식", "enCu", "zhCu", "msCu", "viCu"),
  JAPANESE("일식", "enLu", "zhLu", "msLu", "viLu"),
  WESTERN("양식", "enLu", "zhLu", "msLu", "viLu"),
  SNACK("분식", "enSc", "zhSc", "msSc", "viSc"),
  SOUTH_AMERICAN("남미음식", "enKi", "zhKi", "msKi", "viKi"),
  SOUTHEAST_ASIAN("동남아음식", "enCh", "zhCh", "msCh", "viCh"),
  VEGAN("비건푸드", "enCh", "zhCh", "msCh", "viCh"),
  HALAL("할랄푸드", "enCh", "zhCh", "msCh", "viCh"),
  MEAT_BLACK_PORK("육류/흑돼지", "enCh", "zhCh", "msCh", "viCh"),
  SEAFOOD("해산물", "enCh", "zhCh", "msCh", "viCh"),
  CHICKEN_BURGER("치킨/버거", "enCh", "zhCh", "msCh", "viCh"),
  CAFE_DESSERT("카페/디저트", "enCh", "zhCh", "msCh", "viCh"),
  PUB_FOOD_PUB("펍/요리주점", "enCh", "zhCh", "msCh", "viCh");


  private final String kr;
  private final String en; //영어
  private final String zh; //중국어
  private final String ms; //말레이시아어
  private final String vi; //베트남어

  RestaurantTypeKeyword(String kr, String en, String zh, String ms,
      String vi) {
    this.kr = kr;
    this.en = en;
    this.zh = zh;
    this.ms = ms;
    this.vi = vi;
  }

  public String getValueByLocale(Language language) {
    return switch (language) {
      case KOREAN -> this.getKr();
      case ENGLISH -> this.getEn();
      case CHINESE -> this.getZh();
      case MALAYSIA -> this.getMs();
      case VIETNAMESE -> this.getVi();
    };
  }
}
