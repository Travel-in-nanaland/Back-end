package com.jeju.nanaland.domain.nature.dto;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
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
public class NatureCompositeDto extends CompositeDto {

  private String intro;
  private String details;
  private String amenity;
  private String fee;

  @QueryProjection
  public NatureCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      Language locale, String title, String content, String address, String addressTag,
      String intro,
      String details, String time, String amenity, String fee) {
    super(id, originUrl, thumbnailUrl, contact, locale, title, content, address, addressTag, time);
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
    this.fee = fee;
  }
}
