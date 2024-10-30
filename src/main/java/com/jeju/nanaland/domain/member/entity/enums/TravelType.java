package com.jeju.nanaland.domain.member.entity.enums;

import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.GOOD_VALUE;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.LOCAL_SPOT;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.LUXURY;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.NATURE;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.SENSIBILITY;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.THEME_PARK;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.TOURIST_SPOT;
import static com.jeju.nanaland.domain.member.entity.enums.TravelTypeHashtag.TRADITION;
import static com.jeju.nanaland.global.exception.ErrorCode.REQUEST_VALIDATION_EXCEPTION;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum TravelType {
  NONE(null, null, null, null, null, null),
  GAMGYUL_ICECREAM(
      "감귤아이스크림", "Tangerine Ice Cream", "Ais Krim Mandarin", "柑橘冰淇淋", "Kem quýt",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, SENSIBILITY)),
  GAMGYUL_RICECAKE(
      "감귤 찹쌀떡", "Tangerine Rice Cake", "Mochi Mandarin", "柑橘糯米糕", "Bánh gạo nếp quýt",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, TRADITION)),
  GAMGYUL(
      "감귤", "Tangerine", "Mandarin", "柑橘", "Quýt",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, NATURE)),
  GAMGYUL_CIDER(
      "감귤사이다", "Tangerine Soda", "Sida Mandarin", "柑橘雪碧", "Nước táo quýt",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, THEME_PARK)),

  GAMGYUL_AFFOKATO(
      "감귤 아포가토", "Tangerine Affogato", "Jenis Affogato Mandarin", "柑橘阿芙佳朵", "Affogato quýt",
      Arrays.asList(TOURIST_SPOT, LUXURY, SENSIBILITY)),
  GAMGYUL_HANGWA(
      "감귤한과", "Tangerine Traditional Sweets", "Kuih Tradisional Mandarin", "柑橘油炸蜜果",
      "Bánh truyền thống quýt (Hangwa)",
      Arrays.asList(TOURIST_SPOT, LUXURY, TRADITION)),
  GAMGYUL_JUICE(
      "감귤주스", "Tangerine Juice", "Jus Mandarin", "柑橘果汁", "Nước ép quýt",
      Arrays.asList(TOURIST_SPOT, LUXURY, NATURE)),
  GAMGYUL_CHOCOLATE(
      "감귤 초콜릿", "Tangerine Chocolate", "Coklat Mandarin", "柑橘巧克力", "Sô-cô-la quýt",
      Arrays.asList(TOURIST_SPOT, LUXURY, THEME_PARK)),

  GAMGYUL_COCKTAIL(
      "감귤 칵테일", "Tangerine Cocktail", "Koktel Mandarin", "柑橘鸡尾酒", "Cocktail quýt",
      Arrays.asList(LOCAL_SPOT, LUXURY, SENSIBILITY)),
  TANGERINE_PEEL_TEA(
      "귤피차", "Tangerine Peel Tea", "Teh Kulit Mandarin", "橘皮茶", "Trà vỏ quýt",
      Arrays.asList(LOCAL_SPOT, LUXURY, TRADITION)),
  GAMGYUL_YOGURT(
      "감귤 요거트", "Tangerine Yogurt", "Yogurt Mandarin", "柑橘酸奶", "Sữa chua quýt",
      Arrays.asList(LOCAL_SPOT, LUXURY, NATURE)),
  GAMGYUL_FLATCCINO(
      "감귤 플랫치노", "Tangerine Frappuccino", "Frappuccino Mandarin", "柑橘冰沙", "Flatccino quýt",
      Arrays.asList(LOCAL_SPOT, LUXURY, THEME_PARK)),

  GAMGYUL_LATTE(
      "감귤 라떼", "Tangerine Latte", "Latte Mandarin", "柑橘拿铁", "Latte quýt",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, SENSIBILITY)),
  GAMGYUL_SIKHYE(
      "감귤식혜", "Tangerine Shikhye", "Sikhye Mandarin", "柑橘甜米露", "Sikhye quýt (nước gạo ngọt)",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, TRADITION)),
  GAMGYUL_ADE(
      "감귤에이드", "Tangerine Ade", "Aid Mandarin", "柑橘汽水", "Nước Ade quýt",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, NATURE)),
  GAMGYUL_BUBBLE_TEA(
      "감귤 버블티", "Tapioca Tangerine Tea", "Bubble Tea Mandarin", "柑橘珍珠奶茶",
      "Trà sữa trân châu quýt",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, THEME_PARK));

  private final String kr;
  private final String en; //영어
  private final String ms; //말레이시아어
  private final String zh; //중국어
  private final String vi; //베트남어
  private final List<TravelTypeHashtag> hashtags;

  TravelType(String kr, String en, String ms, String zh, String vi,
      List<TravelTypeHashtag> hashtags) {
    this.kr = kr;
    this.en = en;
    this.ms = ms;
    this.zh = zh;
    this.vi = vi;
    this.hashtags = hashtags;
  }

  public String getTypeNameWithLocale(Language locale) {
    return switch (locale) {
      case KOREAN -> this.kr;
      case ENGLISH -> this.en;
      case CHINESE -> this.zh;
      case MALAYSIA -> this.ms;
      case VIETNAMESE -> this.vi;
    };
  }

  public List<String> getHashtagsWithLanguage(Language language) {

    List<String> result = new ArrayList<>();

    switch (language) {
      case KOREAN -> {
        for (TravelTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getKr());
        }
      }
      case ENGLISH -> {
        for (TravelTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getEn());
        }
      }
      case CHINESE -> {
        for (TravelTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getZh());
        }
      }
      case MALAYSIA -> {
        for (TravelTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getMs());
        }
      }
      case VIETNAMESE -> {
        for (TravelTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getVi());
        }
      }
      default -> throw new BadRequestException(REQUEST_VALIDATION_EXCEPTION.getMessage());
    }

    return result;
  }
}
