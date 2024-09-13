package com.jeju.nanaland.domain.report.entity.claim;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.report.entity.Report;
import com.jeju.nanaland.domain.report.entity.ReportType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
import org.thymeleaf.context.Context;

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
      ClaimType claimType, String content, String email) {
    super(member, email);
    this.referenceId = referenceId;
    this.claimReportType = claimReportType;
    this.claimType = claimType;
    this.content = content;
  }

  @Override
  public ReportType getReportType() {
    return ReportType.CLAIM;
  }

  /**
   * 신고 요청 메일 내용 구성
   *
   * @param message 내용
   * @param context context
   * @throws MessagingException 메일 관련 오류가 발생한 경우
   */
  @Override
  public String setReportContextAndGetTemplate(MimeMessage message, Context context)
      throws MessagingException {
    message.setSubject("[Nanaland] 리뷰 신고 요청입니다.");
    context.setVariable("report_type", this.getClaimReportType());
    context.setVariable("claim_type", this.getClaimType());
    context.setVariable("id", this.getId());
    context.setVariable("content", this.getContent());
    context.setVariable("email", this.getEmail());
    return "claim-report";
  }
}
