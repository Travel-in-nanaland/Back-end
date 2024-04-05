package com.jeju.nanaland.domain.market.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketMarketTransDto {

  private Long id;
  private String originUrl;
  private String thumbnailUrl;
  private String contact;
  private String homepage;
  private String locale;
  private String title;
  private String content;
  private String address;
  private String time;
  private String amenity;

  @QueryProjection
  public MarketMarketTransDto(Long id, String originUrl, String thumbnailUrl, String contact,
      String homepage, String locale, String title, String content, String address, String time,
      String amenity) {
    this.id = id;
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.contact = contact;
    this.homepage = homepage;
    this.locale = locale;
    this.title = title;
    this.content = content;
    this.address = address;
    this.time = time;
    this.amenity = amenity;
  }
}
