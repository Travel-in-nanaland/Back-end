package com.jeju.nanaland.domain.nature.entity;

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
public class NatureTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nature_id", nullable = false)
  private Nature nature;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  private String intro;

  private String details;

  private String amenity;

  private String fee;

  @Builder
  public NatureTrans(Nature nature, Language language, String title, String content, String address,
      String time, String intro,
      String details, String amenity, String fee) {
    super(title, content, address, time);
    this.nature = nature;
    this.language = language;
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
    this.fee = fee;
  }
}
