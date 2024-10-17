package com.jeju.nanaland.domain.common.data;

import lombok.Getter;

@Getter
public enum AddressTag {
  AEWOL("애월", "Aewol", "涯月"),
  SEONGSAN("성산", "Seongsan", "城山"),
  HALLIM("한림", "Hallim", "翰林"),
  JOCHEON("조천", "Jocheon", "朝天"),
  GUJWA("구좌", "Gujwa", "旧左"),
  HANGYEONG("한경", "Hangye-ong", "翰京"),
  DAEJEONG("대정", "Daejeong", "大静"),
  ANDEOK("안덕", "Andeok", "安德"),
  NAMWON("남원", "Namwon", "南元"),
  PYOSEON("표선", "Pyoseon", "表善"),
  UDO("우도", "Udo", "牛岛"),
  CHUJA("추자", "Chuja", "楸子"),
  JEJU("제주시", "Jeju City", "济州市"),
  SEOGWIPO("서귀포시", "Seogw-ipo City", "西归浦市");

  private final String kr;
  private final String en;
  private final String zh;

  AddressTag(String kr, String en, String zh) {
    this.kr = kr;
    this.en = en;
    this.zh = zh;
  }

  public String getValueByLocale(Language locale) {
    return switch (locale) {
      case KOREAN -> this.getKr();
      case ENGLISH, MALAYSIA, VIETNAMESE -> this.getEn();
      case CHINESE -> this.getZh();
    };
  }
}
