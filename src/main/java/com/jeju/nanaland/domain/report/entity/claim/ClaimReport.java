package com.jeju.nanaland.domain.report.entity.claim;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.report.entity.Report;
import com.jeju.nanaland.domain.report.entity.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClaimReport extends Report {

  @NotNull
  private Long referenceId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ClaimReportType claimReportType;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ClaimType claimType;

  @NotBlank
  @Column(nullable = false)
  private String content;

  @Builder
  public ClaimReport(Member member, Long referenceId, ClaimReportType claimReportType,
      ClaimType claimType, String content) {
    super(member);
    this.referenceId = referenceId;
    this.claimReportType = claimReportType;
    this.claimType = claimType;
    this.content = content;
  }

  @Override
  public ReportType getReportType() {
    return ReportType.CLAIM;
  }
}
