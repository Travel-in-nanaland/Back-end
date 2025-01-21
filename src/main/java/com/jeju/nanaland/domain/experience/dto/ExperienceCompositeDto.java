package com.jeju.nanaland.domain.experience.dto;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
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
public class ExperienceCompositeDto extends CompositeDto {

  private String homepage;
  private String intro;
  private String details;
  private String amenity;
  private String fee;
  private ExperienceType experienceType;

  @QueryProjection
  public ExperienceCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      String homepage, Language language, String title, String content, String address,
      String addressTag, String intro, String details, String time, String amenity, String fee,
      ExperienceType experienceType) {
    super(id, originUrl, thumbnailUrl, contact, language, title, content, address, addressTag,
        time);
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
    this.fee = fee;
    this.homepage = homepage;
    this.experienceType = experienceType;
  }
}
