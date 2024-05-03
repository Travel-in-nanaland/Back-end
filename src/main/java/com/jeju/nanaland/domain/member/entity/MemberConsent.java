package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberConsent extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ConsentType consentType;

  @NotNull
  private boolean consent;

  private LocalDateTime consentDate;

  @Builder
  public MemberConsent(Member member, ConsentType consentType, boolean consent,
      LocalDateTime consentDate) {
    this.member = member;
    this.consentType = consentType;
    this.consent = consent;
    this.consentDate = consentDate;
  }
}
