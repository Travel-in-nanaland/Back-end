package com.jeju.nanaland.domain.notification.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotificationCompose {

  private Long id;
  private Long memberId;
  private String contentCategory;
  private Long contentId;

  @QueryProjection
  public MemberNotificationCompose(Long id, Long memberId, String contentCategory, Long contentId) {
    this.id = id;
    this.memberId = memberId;
    this.contentCategory = contentCategory;
    this.contentId = contentId;
  }
}
