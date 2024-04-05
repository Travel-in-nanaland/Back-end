package com.jeju.nanaland.domain.member.entity;

import com.mysema.commons.lang.Pair;
import lombok.Getter;

@Getter
public enum MemberType {
  /**
   * TODO: 매핑 수정
   * 추천 개수가 많아질 경우를 대비해서 배열 형태로 구성했습니다.
   */
  GAMGYUL_ICECREAM(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_RICECAKE(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_CIDER(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),

  GAMGYUL_AFFOKATO(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_HANGWA(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_JUICE(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_CHOCOLATE(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),

  GAMGYUL_COCKTAIL(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  TANGERINE_PEEL_TEA(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_YOGURT(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_FLATCCINO(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),

  GAMGYUL_LATTE(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_SIKHYE(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_ADE(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)}),
  GAMGYUL_BUBBLE_TEA(new Pair[]{new Pair<>("NATURE", 1L), new Pair<>("NATURE", 2L)});

  private final Pair<String, Long>[] recommendPosts;

  MemberType(Pair<String, Long>[] recommendPosts) {
    this.recommendPosts = recommendPosts;
  }
}
