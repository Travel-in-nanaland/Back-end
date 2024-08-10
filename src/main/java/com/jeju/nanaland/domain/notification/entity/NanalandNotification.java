package com.jeju.nanaland.domain.notification.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "notificationUniqueConstraint",
            columnNames = {"notification_category, content_id, title, content"}
        )
    }
)
public class NanalandNotification extends BaseEntity {

  @NotNull
  private NotificationCategory notificationCategory;

  private Long contentId;

  @NotNull
  private String title;

  @NotNull
  private String content;

  public static NanalandNotification buildNanalandNotification(NotificationDto notificationDto) {

    return NanalandNotification.builder()
        .notificationCategory(notificationDto.getCategory())
        .contentId(notificationDto.getContentId())
        .title(notificationDto.getTitle())
        .content(notificationDto.getContent())
        .build();
  }
}
