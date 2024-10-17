package com.jeju.nanaland.domain.nana.entity;

import static com.jeju.nanaland.global.exception.ErrorCode.INFO_TYPE_NOT_FOUND;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.global.exception.NotFoundException;
import lombok.Getter;

@Getter
public enum InfoType {
  ADDRESS("주소", "AddressTag", "Địa chỉ", "Alamat", "地址"),
  PARKING("주차", "Parking", "Chỗ đậu xe", "Tempat Letak Kereta", "停车"),
  SPECIAL("이 장소만의 매력포인트", "Unique Points of This Place", "Điểm hấp dẫn của nơi này",
      "Tarikan Istimewa Tempat Ini",
      "这个地方的独特魅力"),
  SNS("SNS", "SNS", "SNS", "SNS", "SNS"),
  AMENITY("편의시설", "Amenities", "Tiện nghi", "Kemudahan", "设施"),
  WEBSITE("홈페이지", "Website", "Trang chủ", "Website", "主页"),
  RESERVATION_LINK("예약링크", "Booking Link", "Liên kết đặt chỗ", "Pautan Tempahan", "预订链接"),
  AGE("이용연령", "Age Restrictions", "Độ tuổi sử dụng", "Sekatan Umur", "使用年龄"),
  TIME("이용시간", "Operating Hours", "Giờ hoạt động", "Waktu Operasi", "利用时间"),
  FEE("이용요금", "Pricing", "Phí sử dụng", "Harga", "使用费用"),
  DATE("이용날짜", "Available Dates", "Ngày sử dụng", "Tarikh Tersedia", "使用日期"),
  DESCRIPTION("소개", "Overview", "Giới thiệu", "Pengenalan", "介绍"),
  CALL("문의사항", "Inquiries", "Thắc mắc", "Pertanyaan", "咨询事项"),
  ETC("기타 사항", "ETC", "Khác", "dan lain-lain", "等等");


  private final String kr;
  private final String en; //영어
  private final String vi; //베트남어
  private final String ms; //말레이시아어
  private final String zh; //중국어

  InfoType(String kr, String en, String vi, String ms, String zh) {
    this.kr = kr;
    this.en = en;
    this.vi = vi;
    this.ms = ms;
    this.zh = zh;
  }

  public static InfoType contains(String name) {
    for (InfoType infoType : values()) {
      if (infoType.name().equals(name)) {
        return infoType;
      }
    }
    throw new NotFoundException(INFO_TYPE_NOT_FOUND.getMessage());
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
