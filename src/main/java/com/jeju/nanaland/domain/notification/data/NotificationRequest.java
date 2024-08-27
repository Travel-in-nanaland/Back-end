package com.jeju.nanaland.domain.notification.data;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

public class NotificationRequest {

  @Getter
  @Builder
  @Schema(description = "알림 정보")
  public static class NotificationDto {

    @NotNull
    @Schema(description = "알림 내용 카테고리")
    private NotificationCategory category;

    @NotNull
    @Schema(description = "알림 내용 id")
    private Long contentId;

    @NotNull
    @Schema(description = "알림 제목")
    private String title;

    @NotNull
    @Schema(description = "알림 내용")
    private String content;

    @NotNull
    @Schema(description = "언어")
    private Language language;
  }

  @Getter
  @Builder
  @Schema(description = "특정 유저에게 알림 요청")
  public static class NotificationWithTargetDto {

    @NotNull
    @Schema(description = "타겟 memberId")
    private Long memberId;

    @NotNull
    @Schema(description = "알림 정보")
    private NotificationDto notificationDto;
  }
}
