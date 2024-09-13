package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoFixReport extends Report{

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

  @NotBlank
  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "이메일 형식이 올바르지 않습니다.")
  @Column(nullable = false)
  private String email;

  @Builder
  public InfoFixReport(Member member, Long postId, Category category, Language locale, String title,
      FixType fixType, String content, String email) {
    super(member);
    this.postId = postId;
    this.category = category;
    this.locale = locale;
    this.title = title;
    this.fixType = fixType;
    this.content = content;
    this.email = email;
  }

  @Override
  public ReportType getReportType() {
    return ReportType.INFO_FIX;
  }
}
