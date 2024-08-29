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
  // TODO: 언어 별 번역된 타입 값 수정
  NONE(null, null, null, null, null, null),
  GAMGYUL_ICECREAM(
      "감귤 아이스크림", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, SENSIBILITY)),
  GAMGYUL_RICECAKE(
      "감귤 찹쌀떡", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, TRADITION)),
  GAMGYUL(
      "감귤", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, NATURE)),
  GAMGYUL_CIDER(
      "감귤 사이다", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, THEME_PARK)),

  GAMGYUL_AFFOKATO(
      "감귤 아포카토", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, LUXURY, SENSIBILITY)),
  GAMGYUL_HANGWA(
      "감귤 한과", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, LUXURY, TRADITION)),
  GAMGYUL_JUICE(
      "감귤 주스", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, LUXURY, NATURE)),
  GAMGYUL_CHOCOLATE(
      "감귤 초콜릿", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(TOURIST_SPOT, LUXURY, THEME_PARK)),

  GAMGYUL_COCKTAIL(
      "감귤 칵테일", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, LUXURY, SENSIBILITY)),
  TANGERINE_PEEL_TEA(
      "귤피차", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, LUXURY, TRADITION)),
  GAMGYUL_YOGURT(
      "감귤 요거트", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, LUXURY, NATURE)),
  GAMGYUL_FLATCCINO(
      "감귤 플랫치노", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, LUXURY, THEME_PARK)),

  GAMGYUL_LATTE(
      "감귤 라떼", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, SENSIBILITY)),
  GAMGYUL_SIKHYE(
      "감귤 식혜", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, TRADITION)),
  GAMGYUL_ADE(
      "감귤 에이드", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, NATURE)),
  GAMGYUL_BUBBLE_TEA(
      "감귤 버블티", "Mandarin ice cream", "Ais Krim  Mandarin", "zh", "vi",
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
