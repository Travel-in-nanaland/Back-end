package com.jeju.nanaland.domain.festival.entity;

import com.jeju.nanaland.domain.common.entity.CommonTrans;
import com.jeju.nanaland.domain.common.entity.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class FestivalTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Festival festival;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Language language;

  private String price;
}
