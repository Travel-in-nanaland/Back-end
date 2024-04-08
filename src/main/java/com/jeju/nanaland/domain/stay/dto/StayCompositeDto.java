package com.jeju.nanaland.domain.stay.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class StayCompositeDto {

  private Long id;
  private String originUrl;
  private String thumbnailUrl;
  private Integer price;
  private String contact;
  private String homepage;
  private String parking;
  private Float ratingAvg;
  private String locale;
  private String title;
  private String intro;
  private String address;
  private String time;

  @QueryProjection
  public StayCompositeDto(Long id, String originUrl, String thumbnailUrl, Integer price,
      String contact, String homepage, String parking, Float ratingAvg, String locale, String title,
      String intro, String address, String time) {
    this.id = id;
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.price = price;
    this.contact = contact;
    this.homepage = homepage;
    this.parking = parking;
    this.ratingAvg = ratingAvg;
    this.locale = locale;
    this.title = title;
    this.intro = intro;
    this.address = address;
    this.time = time;
  }
}
