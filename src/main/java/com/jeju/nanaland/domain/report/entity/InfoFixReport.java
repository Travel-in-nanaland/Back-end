package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoFixReport extends BaseEntity {

  @NotNull
  private Long postId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Category category;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Locale locale;

  @NotNull
  private String title;

  @NotNull
  @Enumerated(EnumType.STRING)
  private FixType fixType;

  @NotBlank
  @Column(columnDefinition = "TEXT")
  private String content;

  private String imageUrl;

  @NotBlank
  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "이메일 형식이 올바르지 않습니다.")
  @Column(nullable = false)
  private String email;
}
