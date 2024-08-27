package com.jeju.nanaland.domain.notification.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "notificationUniqueConstraint",
            columnNames = {"notification_category", "content_id", "title", "content"}
        )
    }
)
public class NanalandNotification extends BaseEntity {

  @OneToMany(mappedBy = "nanalandNotification", cascade = CascadeType.ALL)
  List<MemberNotification> memberNotificationList;

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

  public NotificationCategory getNotificationCategory() {
    return notificationCategory;
  }

  public Long getContentId() {
    return contentId;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }
}
