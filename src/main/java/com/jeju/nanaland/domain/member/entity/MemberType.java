package com.jeju.nanaland.domain.member.entity;

import static com.jeju.nanaland.domain.common.data.CategoryContent.MARKET;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NATURE;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.mysema.commons.lang.Pair;
import lombok.Getter;

@Getter
public enum MemberType {
  /**
   * TODO: 매핑 수정
   * 추천 개수가 많아질 경우를 대비해서 배열 형태로 구성했습니다.
   */
  GAMGYUL_ICECREAM(new Pair[]{new Pair<>(NATURE, 466L), new Pair<>(NATURE, 477L)}),
  GAMGYUL_RICECAKE(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 487L)}),
  GAMGYUL(new Pair[]{new Pair<>(NATURE, 474L), new Pair<>(NATURE, 445L)}),
  GAMGYUL_CIDER(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(MARKET, 17L)}),

  GAMGYUL_AFFOKATO(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),
  GAMGYUL_HANGWA(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),
  GAMGYUL_JUICE(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),
  GAMGYUL_CHOCOLATE(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),

  GAMGYUL_COCKTAIL(new Pair[]{new Pair<>(NATURE, 133L), new Pair<>(NATURE, 433L)}),
  TANGERINE_PEEL_TEA(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 467L)}),
  GAMGYUL_YOGURT(new Pair[]{new Pair<>(NATURE, 444L), new Pair<>(NATURE, 464L)}),
  GAMGYUL_FLATCCINO(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),

  GAMGYUL_LATTE(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),
  GAMGYUL_SIKHYE(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)}),
  GAMGYUL_ADE(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 472L)}),
  GAMGYUL_BUBBLE_TEA(new Pair[]{new Pair<>(NATURE, 1L), new Pair<>(NATURE, 2L)});

  private final Pair<CategoryContent, Long>[] recommendPosts;

  MemberType(Pair<CategoryContent, Long>[] recommendPosts) {
    this.recommendPosts = recommendPosts;
  }
}
