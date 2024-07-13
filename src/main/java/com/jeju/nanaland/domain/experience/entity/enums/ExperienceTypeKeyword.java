package com.jeju.nanaland.domain.experience.entity.enums;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum ExperienceTypeKeyword {

  LAND_LEISURE(ExperienceType.ACTIVITY, "지상레저", "enAn", "zhAn", "msAn", "viAn"),
  WATER_LEISURE(ExperienceType.ACTIVITY, "수상레저", "enCu", "zhCu", "msCu", "viCu"),
  AIR_LEISURE(ExperienceType.ACTIVITY, "항공레저", "enLu", "zhLu", "msLu", "viLu"),
  MARINE_EXPERIENCE(ExperienceType.ACTIVITY, "해양체험", "enSc", "zhSc", "msSc", "viSc"),
  RURAL_EXPERIENCE(ExperienceType.ACTIVITY, "농총체험", "enKi", "zhKi", "msKi", "viKi"),
  HEALING_THERAPY(ExperienceType.ACTIVITY, "힐링테라피", "enCh", "zhCh", "msCh", "viCh"),

  HISTORY(ExperienceType.CULTURE_AND_ARTS, "역사", "enFr", "zhFr", "msFr", "viFr"),
  EXHIBITION(ExperienceType.CULTURE_AND_ARTS, "전시회", "enPa", "zhPa", "msPa", "viPa"),
  WORKSHOP(ExperienceType.CULTURE_AND_ARTS, "공방", "enAl", "zhAl", "msAl", "viAl"),
  ART_MUSEUM(ExperienceType.CULTURE_AND_ARTS, "미술관", "enHa", "zhHa", "msHa", "viHa"),
  MUSEUM(ExperienceType.CULTURE_AND_ARTS, "박물관", "enRe", "zhRe", "msRe", "viRe"),
  PARK(ExperienceType.CULTURE_AND_ARTS, "공원", "enPe", "zhPe", "msPe", "viPe"),
  PERFORMANCE(ExperienceType.CULTURE_AND_ARTS, "공연", "enOu", "zhOu", "msOu", "viOu"),
  RELIGIOUS_FACILITY(ExperienceType.CULTURE_AND_ARTS, "종교시설", "enLa", "zhLa", "msLa", "viLa"),
  THEME_PARK(ExperienceType.CULTURE_AND_ARTS, "테마파크", "enPa", "zhPa", "msPa", "viPa");

  private final ExperienceType experienceType;
  private final String kr;
  private final String en; //영어
  private final String zh; //중국어
  private final String ms; //말레이시아어
  private final String vi; //베트남어

  ExperienceTypeKeyword(ExperienceType experienceType, String kr, String en, String zh, String ms,
      String vi) {
    this.experienceType = experienceType;
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
