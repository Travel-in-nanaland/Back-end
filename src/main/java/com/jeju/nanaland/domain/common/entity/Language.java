package com.jeju.nanaland.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Language extends BaseEntity {

  @NotBlank
  @Column(nullable = false, unique = true)
  private String locale;

  @NotBlank
  @Column(nullable = false)
  private String dateFormat;
}
