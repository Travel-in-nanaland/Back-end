package com.jeju.nanaland.domain.notification.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEND_NOTIFICATION_SUCCESS;

import com.jeju.nanaland.domain.notification.data.NotificationRequest;
import com.jeju.nanaland.domain.notification.service.NotificationService;
import com.jeju.nanaland.global.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Slf4j
@Tag(name = "알림(notification)", description = "알림(notification) API입니다.")
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/send-all")
  public BaseResponse<String> sendNotificationToAllMember(
      @RequestBody NotificationRequest.FcmMessage fcmMessage) {

    String message = notificationService.sendPushNotification(fcmMessage);
    log.info(message);
    return BaseResponse.success(SEND_NOTIFICATION_SUCCESS);
  }
}
