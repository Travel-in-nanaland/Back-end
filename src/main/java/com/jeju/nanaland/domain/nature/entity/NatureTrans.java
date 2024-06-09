package com.jeju.nanaland.domain.nature.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.Language;
import jakarta.persistence.Column;
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
public class NatureTrans extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nature_id", nullable = false)
  private Nature nature;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String address;

  private String addressTag;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String time;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String intro;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String details;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String amenity;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String fee;

  @Builder
  public NatureTrans(Nature nature, Language language, String title, String content, String address,
      String addressTag, String time, String intro, String details, String amenity, String fee) {
    this.nature = nature;
    this.language = language;
    this.title = title;
    this.content = content;
    this.address = address;
    this.addressTag = addressTag;
    this.time = time;
    this.intro = intro;
    this.details = details;
    this.amenity = amenity;
    this.fee = fee;
  }
}
