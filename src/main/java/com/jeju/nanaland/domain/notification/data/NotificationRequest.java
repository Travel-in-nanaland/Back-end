package com.jeju.nanaland.domain.notification.data;

import com.jeju.nanaland.domain.common.data.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

public class NotificationRequest {

  @Getter
  @Builder
  @Schema(description = "알림 정보")
  public static class FcmMessageDto {

    @NotNull
    @Schema(description = "알림 내용 카테고리")
    private String contentCategory;

    @NotNull
    @Schema(description = "알림 내용 id")
    private Long contentId;

    @NotNull
    @Schema(description = "언어")
    private Language language;

    @NotNull
    @Schema(description = "알림 제목")
    private String title;

    @NotNull
    @Schema(description = "알림 내용")
    private String content;
  }

  @Getter
  @Builder
  @Schema(description = "특정 유저에게 알림 요청")
  public static class FcmMessageToTargetDto {

    @NotNull
    @Schema(description = "타겟 fcm 토큰")
    private String targetToken;

    @NotNull
    @Schema(description = "알림 정보")
    private FcmMessageDto message;
  }
}
