package com.jeju.nanaland.domain.common.entity;

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
public class Language extends BaseEntity {

  @NotNull
  @Column(nullable = false, unique = true)
  @Enumerated(EnumType.STRING)
  private Locale locale;

  @NotBlank
  @Column(nullable = false)
  private String dateFormat;
}
