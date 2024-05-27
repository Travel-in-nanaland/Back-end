package com.jeju.nanaland.domain.member.entity.enums;

import lombok.Getter;

@Getter
public enum MemberTypeHashtag {

  TOURIST_SPOT("관광장소", "Tourist spot", "Tempat pelancongan", "景点", "Điểm du lịch"),
  LOCAL_SPOT("로컬장소", "local spot", "Tempat lokal", "静谧景点", "Địa phương yêu thích"),
  GOOD_VALUE("가성비", "good value", "Berbaloi", "性价比", "Đáng giá"),
  LUXURY("럭셔리", "luxury", "Mewah", "奢华", "Sang trọng"),
  SENSIBILITY("감성", " Sensibility", "Emosi", "情感场所", "Cảm xúc"),
  NATURE("자연", "Nature", "Alam semula jadi", "自然景观", "Thiên nhiên"),
  TRADITION("전통", "Tradition", "Tradisi", "传统文化", "Truyền thống"),
  THEME_PARK("테마파크", "Theme park", "Taman tema", "主题公园", "Công viên giải trí");

  private final String kr;
  private final String en; //영어
  private final String ms; //말레이시아어
  private final String zh; //중국어
  private final String vi; //베트남어

  MemberTypeHashtag(String kr, String en, String ms, String zh, String vi) {
    this.kr = kr;
    this.en = en;
    this.ms = ms;
    this.zh = zh;
    this.vi = vi;
  }
}
