package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.global.exception.ServerErrorException;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum MemberType {
  GAMGYUL_ICECREAM("감귤 아이스크림"),
  GAMGYUL_RICECAKE("감귤 찹쌀떡"),
  GAMGYUL("감귤"),
  GAMGYUL_CIDER("감귤 사이다"),

  GAMGYUL_AFFOKATO("감귤 아포카토"),
  GAMGYUL_HANGWA("감귤 한과"),
  GAMGYUL_JUICE("감귤 주스"),
  GAMGYUL_CHOCOLATE("감귤 초콜릿"),

  GAMGYUL_COCKTAIL("감귤 칵테일"),
  TANGERINE_PEEL_TEA("귤피차"),
  GAMGYUL_YOGURT("감귤 요거트"),
  GAMGYUL_FLATCCINO("감귤 플랫치노"),

  GAMGYUL_LATTE("감귤 라떼"),
  GAMGYUL_SIKHYE("감귤 식혜"),
  GAMGYUL_ADE("감귤 에이드"),
  GAMGYUL_BUBBLE_TEA("감귤 버블티");

  private String memberType;

  MemberType(String memberType) {
    this.memberType = memberType;
  }

  public List<String> getKeywordsByMemberType() {
    if (this.memberType.equals("감귤 아이스크림")) {
      return Arrays.asList("관광장소", "가성비", "감성");

    } else if (this.memberType.equals("감귤 찹쌀떡")) {
      return Arrays.asList("관광장소", "가성비", "전통문화");

    } else if (this.memberType.equals("감귤")) {
      return Arrays.asList("관광장소", "가성비", "자연경관");

    } else if (this.memberType.equals("감귤 사이다")) {
      return Arrays.asList("관광장소", "가성비", "테마파크");

    } else if (this.memberType.equals("감귤 아포카토")) {
      return Arrays.asList("관광장소", "럭셔리", "감성");

    } else if (this.memberType.equals("감귤 한과")) {
      return Arrays.asList("관광장소", "럭셔리", "전통문화");

    } else if (this.memberType.equals("감귤 주스")) {
      return Arrays.asList("관광장소", "럭셔리", "자연경관");

    } else if (this.memberType.equals("감귤 초콜릿")) {
      return Arrays.asList("관광장소", "럭셔리", "테마파크");

    } else if (this.memberType.equals("감귤 칵테일")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "감성");

    } else if (this.memberType.equals("귤피차")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "전통문화");

    } else if (this.memberType.equals("감귤 요거트")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "자연경관");

    } else if (this.memberType.equals("감귤 플랫치노")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "테마파크");

    } else if (this.memberType.equals("감귤 라떼")) {
      return Arrays.asList("한적한 로컬장소", "가성비", "감성");

    } else if (this.memberType.equals("감귤 식혜")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "전통문화");

    } else if (this.memberType.equals("감귤 에이드")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "자연경관");

    } else if (this.memberType.equals("감귤 버블티")) {
      return Arrays.asList("한적한 로컬장소", "럭셔리", "테마파크");

    } else {
      throw new ServerErrorException();
    }
  }
}
