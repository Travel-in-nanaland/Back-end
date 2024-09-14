package com.jeju.nanaland.domain.member.entity;

import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.member.entity.enums.WithdrawalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("status = 'ACTIVE'")
public class MemberWithdrawal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @NotNull
  @Enumerated(EnumType.STRING)
  private WithdrawalType withdrawalType;

  private LocalDateTime withdrawalDate;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "status")
  private Status status = Status.ACTIVE;

  @Builder
  public MemberWithdrawal(Member member, WithdrawalType withdrawalType) {
    this.member = member;
    this.withdrawalType = withdrawalType;
    this.withdrawalDate = LocalDateTime.now();
  }

  public void updateStatus(Status status) {
    this.status = status;
  }

  public void updateWithdrawalDate() {
    this.withdrawalDate = withdrawalDate.minusMonths(4);
  }
}
