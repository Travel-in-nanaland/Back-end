package com.jeju.nanaland.domain.notice.entity;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum NoticeCategory {
  NOTICE("공지 사항", "enNo", "zhNo", "msNo", "viNo"),
  UPDATE("개편 사항", "enUp", "zhUp", "msUp", "viUp");

  private final String kr;
  private final String en; //영어
  private final String zh; //중국어
  private final String ms; //말레이시아어
  private final String vi; //베트남어

  NoticeCategory(String kr, String en, String zh, String ms, String vi) {
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
