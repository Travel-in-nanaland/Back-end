package com.jeju.nanaland.domain.notification.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEND_NOTIFICATION_SUCCESS;

import com.jeju.nanaland.domain.notification.data.NotificationRequest;
import com.jeju.nanaland.domain.notification.service.NotificationService;
import com.jeju.nanaland.global.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

  @Operation(
      summary = "알림 전체 전송",
      description = "모든 유저에게 알림 전송")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "FCM을 통한 알림 전송 실패")
  })
  @PostMapping("/send/all")
  public BaseResponse<String> sendNotificationToAllMember(
      @RequestBody @Valid NotificationRequest.FcmMessageDto fcmMessageDto) {

    notificationService.sendPushNotificationToAllMembers(fcmMessageDto);
    return BaseResponse.success(SEND_NOTIFICATION_SUCCESS);
  }

  @Operation(
      summary = "알림 개별 전송",
      description = "FCM 토큰을 이용하여 특정 유저에게 알림 전송")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "FCM을 통한 알림 전송 실패")
  })
  @PostMapping("/send")
  public BaseResponse<String> sendNotificationToAllMember(
      @RequestBody @Valid NotificationRequest.FcmMessageToTargetDto reqDto) {

    notificationService.sendPushNotificationToTarget(reqDto);
    return BaseResponse.success(SEND_NOTIFICATION_SUCCESS);
  }
}
