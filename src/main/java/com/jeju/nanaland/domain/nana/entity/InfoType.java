package com.jeju.nanaland.domain.nana.entity;

public enum InfoType {
  ADDRESS("주소"),
  PARKING("주차"),
  SPECIAL("스페셜"),
  AMENITY("편의시설"),
  WEBSITE("홈페이지"),
  RESERVATION_LINK("예약링크"),
  AGE("이용연령"),
  TIME("이용시간"),
  FEE("이용요금"),
  DATE("이용날짜"),
  DESCRIPTION("소개");


  private final String description;

  InfoType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
