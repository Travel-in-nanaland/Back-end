package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class NanaAdditionalInfo extends BaseEntity {

  @Enumerated(EnumType.STRING)
  private InfoType infoType;

  private String description;
}
