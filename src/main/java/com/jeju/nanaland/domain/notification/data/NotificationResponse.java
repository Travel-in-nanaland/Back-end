package com.jeju.nanaland.domain.notification.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class NotificationResponse {

  @Getter
  @Builder
  @Schema(description = "알림 리스트 조회 결과")
  public static class NotificationListDto {

    @Schema(description = "총 알림 개수")
    private Long totalElements;

    @Schema(description = "알림 정보 리스트")
    private List<NotificationDetailDto> data;
  }

  @Getter
  @Builder
  @Schema(description = "알림 정보")
  public static class NotificationDetailDto {

    @Schema(description = "알림 id")
    private Long notificationId;

    @Schema(description = "알림 내용 카테고리")
    private String contentCategory;

    @Schema(description = "알림 내용 id")
    private Long contentId;

    @Schema(description = "알림 제목")
    private String title;

    @Schema(description = "알림 내용")
    private String content;

    @Schema(description = "알림 클릭 이벤트")
    private String clickAction;
  }
}
