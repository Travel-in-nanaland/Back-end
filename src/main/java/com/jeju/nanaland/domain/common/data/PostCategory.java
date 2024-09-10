package com.jeju.nanaland.domain.common.data;

import lombok.Getter;

@Getter
public enum PostCategory {
  NANA("나나's Pick", "Nana’s Pick", "Lựa chọn của Nana", "Pilihan Nana", "Nana精选"),
  EXPERIENCE("이색 체험", "Unique experience", "Trải nghiệm độc đáo", "Pengalaman unik", "独特体验"),
  FESTIVAL("축제", "Festivities", "Lễ hội", "Perayaan", "庆典"),
  NATURE("7대 자연", "7 natural wonders", "7 kỳ quan thiên nhiên", "7 keajaiban alam", "七大自然奇观"),
  MARKET("전통시장", "Traditional market", "Chợ truyền thống", "Pasar tradisional", "传统市场"),
  RESTAURANT("제주 맛집", "Restaurants", "Nhà hàng", "Restoran", "餐馆");

  private final String kr;
  private final String en; //영어
  private final String vi; //베트남어
  private final String ms; //말레이시아어
  private final String zh; //중국어

  PostCategory(String kr, String en, String vi, String ms, String zh) {
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
