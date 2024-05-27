package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.CommonTrans;
import com.jeju.nanaland.domain.common.entity.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "experience_id", nullable = false)
  private Experience experience;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  private String intro;

  private String details;

  private String amenity;

  @Builder
  public ExperienceTrans(Experience experience, Language language, String title, String content,
      String address, String addressTag, String time, String intro, String details,
      String amenity) {
    super(title, content, address, addressTag, time);
    this.experience = experience;
    this.language = language;
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
  }
}
