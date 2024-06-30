package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceTrans extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "experience_id", nullable = false)
  private Experience experience;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Language language;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String address;

  private String addressTag;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String time;

  private String intro;

  private String details;

  private String amenity;

  @Builder
  public ExperienceTrans(Experience experience, Language language, String title, String content,
      String address, String addressTag, String time, String intro, String details,
      String amenity) {
    this.experience = experience;
    this.language = language;
    this.title = title;
    this.content = content;
    this.address = address;
    this.addressTag = addressTag;
    this.time = time;
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
  }
}
