package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTravelType extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(unique = true)
  @NotNull
  private TravelType travelType;

  @Builder
  public MemberTravelType(TravelType travelType) {
    this.travelType = travelType;
  }
}
