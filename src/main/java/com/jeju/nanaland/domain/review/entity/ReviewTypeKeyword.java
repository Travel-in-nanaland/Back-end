package com.jeju.nanaland.domain.review.entity;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum ReviewTypeKeyword {

  // TODO : 리뷰 해시태그 번역 별로  수정하기
  ANNIVERSARY(Type.MOOD, "기념일에 가면 좋아요", "enAn", "zhAn", "msAn", "viAn"),
  CUTE(Type.MOOD, "아기자기해요", "enCu", "zhCu", "msCu", "viCu"),
  LUXURY(Type.MOOD, "고급스러워요", "enLu", "zhLu", "msLu", "viLu"),
  SCENERY(Type.MOOD, "풍경이 예뻐요", "enSc", "zhSc", "msSc", "viSc"),
  KIND(Type.MOOD, "친절해요", "enKi", "zhKi", "msKi", "viKi"),

  CHILDREN(Type.COMPANION, "자녀", "enCh", "zhCh", "msCh", "viCh"),
  FRIEND(Type.COMPANION, "친구", "enFr", "zhFr", "msFr", "viFr"),
  PARENTS(Type.COMPANION, "부모님", "enPa", "zhPa", "msPa", "viPa"),
  ALONE(Type.COMPANION, "혼자", "enAl", "zhAl", "msAl", "viAl"),
  HALF(Type.COMPANION, "연인/배우자", "enHa", "zhHa", "msHa", "viHa"),
  RELATIVE(Type.COMPANION, "친척/형제", "enRe", "zhRe", "msRe", "viRe"),
  PET(Type.COMPANION, "반려동물", "enPe", "zhPe", "msPe", "viPe"),

  OUTLET(Type.AMENITIES, "콘센트 사용 가능", "enOu", "zhOu", "msOu", "viOu"),
  LARGE(Type.AMENITIES, "넓은 장소", "enLa", "zhLa", "msLa", "viLa"),
  PARK(Type.AMENITIES, "주차장", "enPa", "zhPa", "msPa", "viPa"),
  BATHROOM(Type.AMENITIES, "깨끗한 화장실", "enBa", "zhBa", "msBa", "viBa");

  private final Type type;
  private final String kr;
  private final String en; //영어
  private final String zh; //중국어
  private final String ms; //말레이시아어
  private final String vi; //베트남어

  ReviewTypeKeyword(Type type, String kr, String en, String zh, String ms, String vi) {
    this.type = type;
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

  public enum Type {
    MOOD, COMPANION, AMENITIES
  }
}
