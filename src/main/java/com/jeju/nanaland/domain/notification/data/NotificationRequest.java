package com.jeju.nanaland.domain.notification.data;

import com.jeju.nanaland.domain.common.data.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

public class NotificationRequest {

  @Getter
  @Builder
  @Schema(name = "알림 정보")
  public static class FcmMessageDto {

    @NotNull
    @Schema(name = "언어")
    private Language language;

    @NotNull
    @Schema(name = "알림 제목")
    private String title;

    @NotNull
    @Schema(name = "알림 내용")
    private String content;

    @NotNull
    @Schema(name = "클릭 시 수행할 행동")
    private String clickAction;
  }

  @Getter
  @Builder
  @Schema(name = "특정 유저에게 알림 요청")
  public static class FcmMessageToTargetDto {

    @NotNull
    @Schema(name = "타겟 fcm 토큰")
    private String targetToken;

    @NotNull
    private FcmMessageDto message;
  }
}
