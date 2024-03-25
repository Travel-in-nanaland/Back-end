package com.jeju.nanaland.domain.nature.entity;

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
public class NatureTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Nature nature;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Language language;

  private String intro;

  private String details;

  private String amenity;
}
