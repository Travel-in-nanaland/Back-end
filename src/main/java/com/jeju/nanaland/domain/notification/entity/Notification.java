package com.jeju.nanaland.domain.notification.entity;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @NotNull
  private String contentCategory;

  private Long contentId;

  @NotNull
  private String title;

  @NotNull
  private String content;

  @NotNull
  private String clickAction;

  @NotNull
  private Language language;
}
