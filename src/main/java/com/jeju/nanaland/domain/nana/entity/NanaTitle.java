package com.jeju.nanaland.domain.nana.entity;

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
public class NanaTitle extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nana_id", nullable = false)
  private Nana nana;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  //이미지 위에 올릴 부제목
  @Column(name = "sub_heading")
  private String subHeading;

  //이미지 위에 올릴 제목
  private String heading;

  private String notice; // 알아두면 좋아요 밑에 들어가는 글, 회의 후 고정이라면 삭제

  @Builder
  public NanaTitle(Nana nana, Language language, String notice,
      String subHeading, String heading) {
    this.nana = nana;
    this.language = language;
    this.notice = notice;
    this.subHeading = subHeading;
    this.heading = heading;
  }
}

