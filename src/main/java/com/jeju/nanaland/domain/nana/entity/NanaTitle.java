package com.jeju.nanaland.domain.nana.entity;

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
public class NanaTitle extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nana_id", nullable = false)
  private Nana nana;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Language language;

  //이미지 위에 올릴 부제목
  @Column(name = "sub_heading")
  private String subHeading;

  //이미지 위에 올릴 제목
  private String heading;

  @Column(columnDefinition = "VARCHAR(1024)")
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

