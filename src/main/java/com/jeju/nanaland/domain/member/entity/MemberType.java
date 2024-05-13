package com.jeju.nanaland.domain.member.entity;

import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.GOOD_VALUE;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.LOCAL_SPOT;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.LUXURY;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.NATURE;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.SENSIBILITY;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.THEME_PARK;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.TOURIST_SPOT;
import static com.jeju.nanaland.domain.member.entity.MemberTypeHashtag.TRADITION;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.mysema.commons.lang.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum MemberType {
  /**
   * TODO: 매핑 수정
   * 추천 개수가 많아질 경우를 대비해서 배열 형태로 구성했습니다.
   */
  GAMGYUL_ICECREAM(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, SENSIBILITY)),
  GAMGYUL_RICECAKE(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, TRADITION)),
  GAMGYUL(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, NATURE)),
  GAMGYUL_CIDER(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, GOOD_VALUE, THEME_PARK)),

  GAMGYUL_AFFOKATO(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, LUXURY, SENSIBILITY)),
  GAMGYUL_HANGWA(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, LUXURY, TRADITION)),
  GAMGYUL_JUICE(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, LUXURY, NATURE)),
  GAMGYUL_CHOCOLATE(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(TOURIST_SPOT, LUXURY, THEME_PARK)),

  GAMGYUL_COCKTAIL(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, LUXURY, SENSIBILITY)),
  TANGERINE_PEEL_TEA(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, LUXURY, TRADITION)),
  GAMGYUL_YOGURT(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, LUXURY, NATURE)),
  GAMGYUL_FLATCCINO(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, LUXURY, THEME_PARK)),

  GAMGYUL_LATTE(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, SENSIBILITY)),
  GAMGYUL_SIKHYE(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, TRADITION)),
  GAMGYUL_ADE(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, NATURE)),
  GAMGYUL_BUBBLE_TEA(
      new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)},
      Arrays.asList(LOCAL_SPOT, GOOD_VALUE, THEME_PARK));

  private final Pair<String, Long>[] recommendPosts;
  private final List<MemberTypeHashtag> hashtags;

  MemberType(Pair<String, Long>[] recommendPosts, List<MemberTypeHashtag> hashtags) {
    this.recommendPosts = recommendPosts;
    this.hashtags = hashtags;
  }

  public List<String> getHashtagsWithLocale(Locale locale) {

    List<String> result = new ArrayList<>();

    switch (locale) {
      case KOREAN -> {
        for (MemberTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getKr());
        }
      }
      case ENGLISH -> {
        for (MemberTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getEn());
        }
      }
      case CHINESE -> {
        for (MemberTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getZh());
        }
      }
      case MALAYSIA -> {
        for (MemberTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getMs());
        }
      }
      case VIETNAMESE -> {
        for (MemberTypeHashtag hashtag : hashtags) {
          result.add(hashtag.getVi());
        }
      }
    }

    return result;
  }
}
