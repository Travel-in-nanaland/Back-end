package com.jeju.nanaland.domain.common.data;

import lombok.Getter;

@Getter
public enum AddressTag {
  AEWOL("애월", "Aewol", "涯月", "제주시 애월읍", "Aewol-eup, Jeju-si"),
  SEONGSAN("성산", "Seongsan", "城山", "서귀포시 성산읍", "Seongsan-eup, Seogwipo-si"),
  HALLIM("한림", "Hallim", "翰林", "제주시 한림읍", "Hallim-eup, Jeju-si"),
  JOCHEON("조천", "Jocheon", "朝天", "제주시 조천읍", "Jocheon-eup, Jeju-si"),
  GUJWA("구좌", "Gujwa", "旧左", "제주시 구좌읍", "Gujwa-eup, Jeju-si"),
  HANGYEONG("한경", "Hangye-ong", "翰京", "제주시 한경면", "Hangyeong-myeon, Jeju-si"),
  DAEJEONG("대정", "Daejeong", "大静", "제주시 대정읍", "Daejeong-eup, Seogwipo-si"),
  ANDEOK("안덕", "Andeok", "安德", "서귀포시 안덕면", "Andeok-myeon, Seogwipo-si"),
  NAMWON("남원", "Namwon", "南元", "서귀포시 남원읍", "Namwon-eup, Seogwipo-si"),
  PYOSEON("표선", "Pyoseon", "表善", "서귀포시 표선면", "Pyoseon-myeon, Seogwipo-si"),
  UDO("우도", "Udo", "牛岛", "제주시 우도면", "Udo-myeon, Jeju-si"),
  CHUJA("추자", "Chuja", "楸子", "제주시 추자면", "Chuja-myeon, Jeju-si"),
  JEJU("제주시", "Jeju City", "济州市", "제주도 제주시", "Jeju-si, Jeju-do"),
  SEOGWIPO("서귀포시", "Seogw-ipo City", "西归浦市", "제주도 서귀포시", "Seogwipo-si, Jeju-do");

  private final String kr;
  private final String en;
  private final String zh;
  private final String krLong;
  private final String enLong;

  AddressTag(String kr, String en, String zh, String krLong, String enLong) {
    this.kr = kr;
    this.en = en;
    this.zh = zh;
    this.krLong = krLong;
    this.enLong = enLong;
  }


  public String getValueByLocale(Language locale) {
    return switch (locale) {
      case KOREAN -> this.getKr();
      case ENGLISH, MALAYSIA, VIETNAMESE -> this.getEn();
      case CHINESE -> this.getZh();
    };
  }

  public String getLongValueByLocale(Language locale) {
    if (locale.equals(Language.KOREAN)) {
      return this.getKrLong();
    } else {
      return this.getEnLong();
    }
  }

  public static AddressTag getAddressTagEnum(String addressTagString) {
    for (AddressTag addressTag : AddressTag.values()) {
      // 여러 언어 값들을 비교하여 enum 값을 찾는다.
      if (addressTag.getKr().equals(addressTagString) ||
          addressTag.getEn().equals(addressTagString) ||
          addressTag.getZh().equals(addressTagString)) {
        return addressTag;
      }
    }
    // 일치하는 값이 없으면 예외 처리 (필요에 따라 예외를 던지거나 기본 값을 반환)
    throw new IllegalArgumentException("Unknown address tag: " + addressTagString);
  }
}