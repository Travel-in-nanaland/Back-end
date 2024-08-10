package com.jeju.nanaland.domain.notification.data;

import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberNotificationCompose {

  private Long id;
  private Long memberId;
  private String category;
  private Long contentId;

  @QueryProjection
  public MemberNotificationCompose(Long id, Long memberId,
      NotificationCategory notificationCategory,
      Long contentId) {
    this.id = id;
    this.memberId = memberId;
    this.category = notificationCategory.name();
    this.contentId = contentId;
  }
}
