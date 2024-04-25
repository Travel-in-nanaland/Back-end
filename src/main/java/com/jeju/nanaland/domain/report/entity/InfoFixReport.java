package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @Enumerated(EnumType.STRING)
  private CategoryContent category;

  @NotNull
  @Enumerated(EnumType.STRING)
  private FixType fixType;

  @NotBlank
  @Column(columnDefinition = "TEXT")
  private String content;

  private String imageUrl;

  @NotBlank
  private String email;
}
