package com.jeju.nanaland.domain.experience.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceExperienceTransDto {

  private Long id;
  private String originUrl;
  private String thumbnailUrl;
  private String contact;
  private Float ratingAvg;
  private String locale;
  private String title;
  private String content;
  private String address;
  private String intro;
  private String details;
  private String time;
  private String amenity;

  @QueryProjection
  public ExperienceExperienceTransDto(Long id, String originUrl, String thumbnailUrl,
      String contact,
      Float ratingAvg, String locale, String title, String content, String address, String intro,
      String details, String time, String amenity) {
    this.id = id;
    this.originUrl = originUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.contact = contact;
    this.ratingAvg = ratingAvg;
    this.locale = locale;
    this.title = title;
    this.content = content;
    this.address = address;
    this.intro = intro;
    this.details = details;
    this.time = time;
    this.amenity = amenity;
  }
}
