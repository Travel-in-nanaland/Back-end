package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nana extends BaseEntity {

  @NotBlank
  @Column(nullable = false)
  private String version;

  // 사용자한테 보여줄 게시물인가 판별
  private boolean active;

  @Builder
  public Nana(String version, boolean active) {
    this.version = version;
    this.active = active;
  }
}


