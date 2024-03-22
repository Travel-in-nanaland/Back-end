package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.CommonTrans;
import com.jeju.nanaland.domain.common.entity.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ExperienceTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Experience experience;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Language language;

  private String intro;

  private String details;

  private String amenity;

  @Builder
  public ExperienceTrans(String title, String content, String address, String time, String intro,
      String details, String amenity) {
    super(title, content, address, time);
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
  }
}
