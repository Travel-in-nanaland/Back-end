package com.jeju.nanaland.domain.notification.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.NOTIFICATION_LIST_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.SEND_NOTIFICATION_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest;
import com.jeju.nanaland.domain.notification.data.NotificationResponse;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationListDto;
import com.jeju.nanaland.domain.notification.service.NotificationService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
      summary = "사용자 알림 리스트 조회",
      description = "사용자 알림 리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<NotificationResponse.NotificationListDto> getNotificationList(
      @AuthMember MemberInfoDto memberInfoDto,
      int page, int size) {

    NotificationListDto notificationListDto = notificationService.getNotificationList(memberInfoDto,
        page, size);
    return BaseResponse.success(NOTIFICATION_LIST_SUCCESS, notificationListDto);
  }

  @Operation(
      summary = "알림 전체 전송 - ADMIN 권한의 토큰 필요",
      description = "모든 유저에게 알림 전송 - ADMIN 권한의 토큰 필요")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "FCM을 통한 알림 전송 실패")
  })
  @PostMapping("/send/all")
  public BaseResponse<String> sendNotificationToAllMember(
      @RequestBody @Valid NotificationRequest.NotificationDto notificationDto) {

    notificationService.sendPushNotificationToAllMembers(notificationDto);
    return BaseResponse.success(SEND_NOTIFICATION_SUCCESS);
  }

  @Operation(
      summary = "알림 개별 전송 - ADMIN 권한의 토큰 필요",
      description = "FCM 토큰을 이용하여 특정 유저에게 알림 전송 - ADMIN 권한의 토큰 필요")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "FCM을 통한 알림 전송 실패")
  })
  @PostMapping("/send")
  public BaseResponse<String> sendNotificationToAllMember(
      @RequestBody @Valid NotificationRequest.NotificationWithTargetDto reqDto) {

    notificationService.sendPushNotificationToTarget(reqDto);
    return BaseResponse.success(SEND_NOTIFICATION_SUCCESS);
  }
}
