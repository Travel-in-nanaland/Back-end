package com.jeju.nanaland.domain.festival.entity;

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
public class FestivalTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "festival_id", nullable = false)
  private Festival festival;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  private String fee;

  @Builder
  public FestivalTrans(Festival festival, Language language, String title, String content,
      String address, String time, String fee) {
    super(title, content, address, time);
    this.festival = festival;
    this.language = language;
    this.fee = fee;
  }
}
