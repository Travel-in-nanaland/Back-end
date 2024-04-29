package com.jeju.nanaland.domain.experience.dto;

import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceCompositeDto extends CompositeDto {

  private Float ratingAvg;
  private String intro;
  private String details;
  private String amenity;

  @QueryProjection
  public ExperienceCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      Float ratingAvg, Locale locale, String title, String content, String address,
      String addressTag, String intro, String details, String time, String amenity) {
    super(id, originUrl, thumbnailUrl, contact, locale, title, content, address, addressTag, time);
    this.ratingAvg = ratingAvg;
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
  }
}
