package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.entity.Member;
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
public class InfoFixReport extends Report {

  @NotNull
  private Long postId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Category category;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Language locale;

  @NotNull
  private String title;

  @NotNull
  @Enumerated(EnumType.STRING)
  private FixType fixType;

  @NotBlank
  @Column(columnDefinition = "TEXT")
  private String content;

  @Builder
  public InfoFixReport(Member member, Long postId, Category category, Language locale, String title,
      FixType fixType, String content, String email) {
    super(member, email);
    this.postId = postId;
    this.category = category;
    this.locale = locale;
    this.title = title;
    this.fixType = fixType;
    this.content = content;
  }

  @Override
  public ReportType getReportType() {
    return ReportType.INFO_FIX;
  }

  /**
   * 정보 수정 제안 요청 메일 내용 구성
   *
   * @param message 내용
   * @param context context
   * @throws MessagingException 메일 관련 오류가 발생한 경우
   */
  @Override
  public String setReportContextAndGetTemplate(MimeMessage message, Context context)
      throws MessagingException {
    message.setSubject("[Nanaland] 정보 수정 요청입니다.");
    context.setVariable("fix_type", this.getFixType());
    context.setVariable("category", this.getCategory());
    context.setVariable("language", this.getLocale().name());
    context.setVariable("title", this.getTitle());
    context.setVariable("content", this.getContent());
    context.setVariable("email", this.getEmail());
    return "info-fix-report";
  }
}
