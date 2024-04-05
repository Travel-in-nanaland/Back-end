package com.jeju.nanaland.domain.member.entity;

import lombok.Getter;

@Getter
public enum MemberType {
  /**
   * TODO: 매핑 수정
   */
  GAMGYUL_ICECREAM("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_RICECAKE("NATURE", 1L, "NATURE", 2L),
  GAMGYUL("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_CIDER("NATURE", 1L, "NATURE", 2L),

  GAMGYUL_AFFOKATO("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_HANGWA("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_JUICE("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_CHOCOLATE("NATURE", 1L, "NATURE", 2L),

  GAMGYUL_COCKTAIL("NATURE", 1L, "NATURE", 2L),
  TANGERINE_PEEL_TEA("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_YOGURT("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_FLATCCINO("NATURE", 1L, "NATURE", 2L),

  GAMGYUL_LATTE("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_SIKHYE("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_ADE("NATURE", 1L, "NATURE", 2L),
  GAMGYUL_BUBBLE_TEA("NATURE", 1L, "NATURE", 2L);

  private String postCategory1;
  private Long postId1;
  private String postCategory2;
  private Long postId2;

  MemberType(String postCategory1, Long postId1, String postCategory2, Long postId2) {
    this.postCategory1 = postCategory1;
    this.postId1 = postId1;
    this.postCategory2 = postCategory2;
    this.postId2 = postId2;
  }
}
