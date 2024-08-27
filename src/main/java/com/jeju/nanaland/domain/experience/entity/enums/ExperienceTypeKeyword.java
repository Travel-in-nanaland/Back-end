package com.jeju.nanaland.domain.experience.entity.enums;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum ExperienceTypeKeyword {

  LAND_LEISURE(ExperienceType.ACTIVITY, "지상레저", "Ground leisure", "陆地休闲", "Rekreasi darat",
      "Giải trí trên mặt đất"),
  WATER_LEISURE(ExperienceType.ACTIVITY, "수상레저", "Water leisure", "水上休闲", "Rekreasi air",
      "Giải trí dưới nước"),
  AIR_LEISURE(ExperienceType.ACTIVITY, "항공레저", "Aviation leisure", "航空休闲",
      "Rekreasi penerbangan", "Giải trí hàng không"),
  MARINE_EXPERIENCE(ExperienceType.ACTIVITY, "해양체험", "Marine experience", "海洋体验",
      "Pengalaman marin", "Trải nghiệm biển"),
  RURAL_EXPERIENCE(ExperienceType.ACTIVITY, "농촌체험", "Rural experience", "农村体验",
      "Pengalaman luar bandar", "Trải nghiệm nông thôn"),
  HEALING_THERAPY(ExperienceType.ACTIVITY, "힐링테라피", "Healing Therapy", "治愈疗法",
      "Terapi Penyembuhan", "Liệu pháp chữa lành"),

  HISTORY(ExperienceType.CULTURE_AND_ARTS, "역사", "History", "历史", "Sejarah", "Lịch sử"),
  EXHIBITION(ExperienceType.CULTURE_AND_ARTS, "전시회", "Exhibition", "展览", "Pameran", "Triển lãm"),
  WORKSHOP(ExperienceType.CULTURE_AND_ARTS, "공방", "Experience Workshop", "体验工艺坊",
      "Bengkel Pengalaman", "Hội thảo trải nghiệm"),
  ART_MUSEUM(ExperienceType.CULTURE_AND_ARTS, "미술관", "Art gallery", "美术馆", "Galeri seni",
      "Phòng trưng bày nghệ thuật"),
  MUSEUM(ExperienceType.CULTURE_AND_ARTS, "박물관", "Museum", "博物馆", "Muzium", "Bảo tàng"),
  PARK(ExperienceType.CULTURE_AND_ARTS, "공원", "Park", "公园", "Taman", "Công viên"),
  PERFORMANCE(ExperienceType.CULTURE_AND_ARTS, "공연", "Performance", "表演", "Persembahan",
      "Biểu diễn"),
  RELIGIOUS_FACILITY(ExperienceType.CULTURE_AND_ARTS, "종교시설", "Religious facilities", "宗教设施",
      "Kemudahan agama", "Cơ sở tôn giáo"),
  THEME_PARK(ExperienceType.CULTURE_AND_ARTS, "테마파크", "Theme park", "主题公园", "Taman tema",
      "Công viên giải trí");

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
