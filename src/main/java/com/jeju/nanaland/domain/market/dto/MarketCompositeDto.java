package com.jeju.nanaland.domain.market.dto;

import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MarketCompositeDto extends CompositeDto {

  private String homepage;
  private String intro;
  private String amenity;

  @QueryProjection
  public MarketCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      String homepage, Locale locale, String title, String content, String address,
      String addressTag, String time, String intro, String amenity) {
    super(id, originUrl, thumbnailUrl, contact, locale, title, content, address, addressTag, time);
    this.homepage = homepage;
    this.intro = intro;
    this.amenity = amenity;
  }
}
