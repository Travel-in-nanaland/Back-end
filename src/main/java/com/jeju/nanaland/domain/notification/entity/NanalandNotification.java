package com.jeju.nanaland.domain.notification.entity;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationClickEvent;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NanalandNotification extends BaseEntity {

  @NotNull
  private NotificationClickEvent clickEvent;

  @NotNull
  private NotificationCategory notificationCategory;

  private Long contentId;

  @NotNull
  private String title;

  @NotNull
  private String content;

  @NotNull
  private Language language;

  public static NanalandNotification buildNanalandNotification(NotificationDto notificationDto) {

    return NanalandNotification.builder()
        .clickEvent(notificationDto.getCategory().getClickEvent())
        .notificationCategory(notificationDto.getCategory())
        .contentId(notificationDto.getContentId())
        .title(notificationDto.getTitle())
        .content(notificationDto.getContent())
        .language(notificationDto.getLanguage())
        .build();
  }
}
