package com.jeju.nanaland.domain.restaurant.entity.enums;

import com.jeju.nanaland.domain.common.data.Language;
import lombok.Getter;

@Getter
public enum RestaurantTypeKeyword {

  KOREAN("한식", "Korean food", "韩餐", "Makanan Korea", "Ẩm thực Hàn Quốc"),
  CHINESE("중식", "Chinese food", "中餐", "Makanan Cina", "Ẩm thực Trung Quốc"),
  JAPANESE("일식", "Japanese food", "日本料理", "Makanan Jepun", "Ẩm thực Nhật Bản"),
  WESTERN("양식", "Western food", "西餐", "Makanan barat", "Món ăn Tây"),
  SNACK("분식", "Snack food", "小吃", "Makanan ringan", "Đồ ăn vặt"),
  SOUTH_AMERICAN("남미음식", "South American food", "南美食物", "Makanan Amerika Selatan",
      "Ẩm thực Nam Mỹ"),
  SOUTHEAST_ASIAN("동남아음식", "Southeast Asian food", "东南亚食物", "Makanan Asia Tenggara",
      "Ẩm thực Đông Nam Á"),
  VEGAN("비건푸드", "Vegan food", "素食", "Makanan Vegan", "Thực phẩm thuần chay"),
  HALAL("할랄푸드", "Halal food", "清真食品", "Makanan Halal", "Thực phẩm Halal"),
  MEAT_BLACK_PORK("육류/흑돼지", "Meat/Black pork", "肉类/黑猪肉", "Daging/Babi hitam",
      "Thịt/Thịt lợn đen"),
  SEAFOOD("해산물", "Seafood", "海鲜", "Makanan laut", "Hải sản"),
  CHICKEN_BURGER("치킨/버거", "Chicken/Burger", "炸鸡/汉堡", "Ayam/Burger", "Gà/Burger"),
  CAFE_DESSERT("카페/디저트", "Cafe/Dessert", "咖啡馆/甜点", "Kafe/Pencuci mulut",
      "Quán cà phê/Món tráng miệng"),
  PUB_FOOD_PUB("펍/요리주점", "Pub/Restaurant", "酒吧/餐厅", "Pub/Restoran", "Quán rượu/Nhà hàng");

  private final String kr;
  private final String en; //영어
  private final String zh; //중국어
  private final String ms; //말레이시아어
  private final String vi; //베트남어

  RestaurantTypeKeyword(String kr, String en, String zh, String ms,
      String vi) {
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
