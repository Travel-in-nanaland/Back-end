package com.jeju.nanaland.domain.notification.data;

import lombok.Builder;
import lombok.Getter;

public class NotificationRequest {

  @Getter
  @Builder
  public static class FcmMessage {

    private String targetToken;
    private String title;
    private String content;
  }
}
