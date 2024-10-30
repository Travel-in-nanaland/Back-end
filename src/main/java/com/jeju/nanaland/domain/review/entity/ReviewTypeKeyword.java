package com.jeju.nanaland.domain.review.entity;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum ReviewTypeKeyword {

  ANNIVERSARY(Type.MOOD, "기념일에 가면 좋아요", "Good for anniversaries", "适合于纪念日", "Sesuai untuk ulang tahun", "Phù hợp cho kỷ niệm"),
  CUTE(Type.MOOD, "아기자기해요", "It's cute", "小巧可爱", "Ia comel", "Nó dễ thương"),
  LUXURY(Type.MOOD, "고급스러워요", "It's luxurious", "很高档", "Ia mewah", "Nó sang trọng"),
  SCENERY(Type.MOOD, "풍경이 예뻐요", "The scenery is pretty", "风景很美", "Pemandangannya cantik", "Phong cảnh đẹp"),
  KIND(Type.MOOD, "친절해요", "You are kind.", "好亲切", "Anda baik hati.", "Bạn rất tốt bụng."),

  CHILDREN(Type.COMPANION, "자녀", "Children", "子女", "Kanak-kanak", "Trẻ em"),
  FRIEND(Type.COMPANION, "친구", "Friend", "朋友", "Kawan", "Bạn bè"),
  PARENTS(Type.COMPANION, "부모님", "Parents", "父母", "Ibu bapa", "Cha mẹ"),
  ALONE(Type.COMPANION, "혼자", "Alone", "单独", "Bersendirian", "Một mình"),
  HALF(Type.COMPANION, "연인/배우자", "Lover/Spouse", "爱人/配偶", "Kekasih/Pasangan", "Người yêu/Vợ chồng"),
  RELATIVE(Type.COMPANION, "친척/형제", "Relative/Sibling", "亲戚/兄弟姐妹", "Saudara-mara/Adik-beradik", "Người thân/Anh chị em"),
  PET(Type.COMPANION, "반려동물", "Pet", "宠物", "Haiwan peliharaan", "Thú cưng"),

  OUTLET(Type.AMENITIES, "콘센트 사용 가능", "Outlet available", "提供电源插座", "Soket tersedia", "Có ổ cắm điện"),
  LARGE(Type.AMENITIES, "넓은 장소", "Wide", "宽敞", "Luas", "Rộng rãi"),
  PARK(Type.AMENITIES, "주차장", "Parking lot", "停车场", "Tempat letak kereta", "Bãi đậu xe"),
  BATHROOM(Type.AMENITIES, "깨끗한 화장실", "Clean toilet", "洁净的卫生间", "Tandas bersih", "Nhà vệ sinh sạch sẽ");

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
