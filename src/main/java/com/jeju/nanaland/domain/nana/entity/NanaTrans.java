package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
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
public class NanaTrans extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Nana nana;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Language language;

  private String content;
}
