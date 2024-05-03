package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberConsents extends BaseEntity {

  @Enumerated(EnumType.STRING)
  private ConsentType consentType;

  private boolean consent;

  private LocalDateTime consentDate;

  @Builder
  public MemberConsents(ConsentType consentType, boolean consent, LocalDateTime consentDate) {
    this.consentType = consentType;
    this.consent = consent;
    this.consentDate = consentDate;
  }
}
