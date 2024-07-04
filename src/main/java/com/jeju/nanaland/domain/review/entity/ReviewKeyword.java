package com.jeju.nanaland.domain.review.entity;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum ReviewKeyword {

  HISTORY(Type.CULTURE_ARTS, "역사", "enHi", "zhHi", "msHi", "viHi"),
  EXHIBITION(Type.CULTURE_ARTS, "전시회", "enEx", "zhEx", "msEx", "viEx"),
  ATELIER(Type.CULTURE_ARTS, "공방", "enAt", "zhAt", "msAt", "viAt"),
  ART_GALLERY(Type.CULTURE_ARTS, "미술관", "enAr", "zhAr", "msAr", "viAr"),
  MUSEUM(Type.CULTURE_ARTS, "박물관", "enMu", "zhMu", "msMu", "viMu"),
  PARK(Type.CULTURE_ARTS, "공원", "enPa", "zhPa", "msPa", "viPa"),
  MEMORIAL_HALL(Type.CULTURE_ARTS, "기념관", "enMe", "zhMe", "msMe", "viMe"),
  RELIGION(Type.CULTURE_ARTS, "종교 시설", "enRe", "zhRe", "msRe", "viRe"),
  THEME_PARK(Type.CULTURE_ARTS, "테마파크", "enTh", "zhTh", "msTh", "viTh"),

  TRACK_FIELD(Type.ACTIVITIES, "육상 스포츠", "enTr", "zhTr", "msTr", "viTr"),
  GROUND_LEISURE(Type.ACTIVITIES, "지상 레저", "enGr", "zhGr", "msGr", "viGr"),
  WATER_LEISURE(Type.ACTIVITIES, "수상 레저", "enWa", "zhWa", "msWa", "viWa"),
  AIR_SPORTS(Type.ACTIVITIES, "항공 스포츠", "enAr", "zhAr", "msAr", "viAr"),
  MARINE_SPORTS(Type.ACTIVITIES, "해상 체험", "enMr", "zhMr", "msMr", "viMr"),

  PARENTS(Type.COMPANION, "부모님", "enPa", "zhPa", "msPa", "viPa"),
  FRIEND(Type.COMPANION, "친구", "enFr", "zhFr", "msFr", "viFr"),
  CHILDREN(Type.COMPANION, "자녀", "enCh", "zhCh", "msCh", "viCh"),
  ALONE(Type.COMPANION, "혼자", "enAl", "zhAl", "msAl", "viAl"),
  HALF(Type.COMPANION, "연인, 배우자", "enHa", "zhHa", "msHa", "viHa"),
  RELATIVE(Type.COMPANION, "형제, 친척", "enRe", "zhRe", "msRe", "viRe"),
  PET(Type.COMPANION, "반려동물", "enPe", "zhPe", "msPe", "viPe");

  private final Type type;
  private final String kr;
  private final String en; //영어
  private final String zh; //중국어
  private final String ms; //말레이시아어
  private final String vi; //베트남어

  ReviewKeyword(Type type, String kr, String en, String zh, String ms, String vi) {
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
    CULTURE_ARTS, ACTIVITIES, COMPANION
  }
}
