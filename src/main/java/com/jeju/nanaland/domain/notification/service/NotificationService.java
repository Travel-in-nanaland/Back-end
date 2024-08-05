package com.jeju.nanaland.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.FcmMessageDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.FcmMessageToTargetDto;
import com.jeju.nanaland.domain.notification.data.NotificationResponse;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationDetailDto;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationListDto;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import com.jeju.nanaland.domain.notification.repository.NotificationRepository;
import com.jeju.nanaland.domain.notification.util.FcmTokenUtil;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final FcmTokenUtil fcmTokenUtil;
  private final NotificationRepository notificationRepository;
  private final FcmTokenRepository fcmTokenRepository;

  public NotificationResponse.NotificationListDto getNotificationList(MemberInfoDto memberInfoDto,
      String fcmToken, int page, int size) {

    // 해당 토큰 조회
    fcmTokenRepository.findByToken(fcmToken)
        .orElseThrow(() -> new NotFoundException("해당 토큰 정보가 없습니다."));

    // 알림 리스트 조회
    Pageable pageable = PageRequest.of(page, size);
    Page<com.jeju.nanaland.domain.notification.entity.Notification> resultPage =
        notificationRepository.findAllNotificationByMember(memberInfoDto.getMember(), pageable);

    return NotificationListDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(resultPage.getContent()
            .stream()
            .map(notification -> NotificationDetailDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .clickAction(notification.getClickAction())
                .build()
            ).toList()
        ).build();
  }

  @Transactional
  public void sendPushNotificationToAllMembers(FcmMessageDto fcmMessageDto) {

    Language language = fcmMessageDto.getLanguage();

    // 요청 언어와 동일한 모든 토큰 조회, 검증
    List<String> verifiedTokenList = fcmTokenRepository.findAllByLanguage(language)
        .stream()
        .filter(fcmToken -> {
          try {
            fcmTokenUtil.verifyFcmToken(fcmToken);
            return true;
          } catch (FirebaseException e) {
            fcmTokenUtil.deleteFcmToken(fcmToken);
            log.info("Invalid fcm token deleted: {}", fcmToken.getToken());
            return false;
          }
        })
        .map(FcmToken::toString)
        .toList();

    // 한번에 전송 가능한 단위인 500개로 분할
    List<List<String>> splitedTokenList = splitTokenList(verifiedTokenList);

    int currentSuccessCount = 0;
    for (List<String> tokenList : splitedTokenList) {
      // 메세지 만들기
      MulticastMessage message = makeMulticastMessage(tokenList, fcmMessageDto);

      // 비동기 메세지 전송
      try {
        ApiFuture<BatchResponse> responseApiFuture = FirebaseMessaging.getInstance()
            .sendEachForMulticastAsync(message);
        BatchResponse batchResponse = responseApiFuture.get();
        currentSuccessCount += batchResponse.getSuccessCount();
        log.info("전체 알림 전송 성공: {}개", currentSuccessCount);
      }
      // ApiFuture 에러 처리
      catch (ExecutionException e) {
        log.error("알림 전송 실패: {}", e.getMessage());
        throw new RuntimeException(e);
      }
      // 쓰레드 인터럽트 에러 처리
      catch (InterruptedException e) {
        log.error("알림 전송 실패: {}", e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  @Transactional
  public void sendPushNotificationToTarget(FcmMessageToTargetDto fcmMessageToTargetDto) {

    String targetToken = fcmMessageToTargetDto.getTargetToken();
    // 타겟 토큰 조회
    FcmToken fcmToken = fcmTokenRepository.findByToken(targetToken)
        .orElseThrow(() -> new NotFoundException("해당 토큰 정보가 없습니다."));

    // FCM 토큰 검증
    try {
      fcmTokenUtil.verifyFcmToken(fcmToken);
    } catch (FirebaseException e) {
      log.error("FCM 토큰 검증 실패: {}", fcmToken.getToken());
      // 해당 토큰 삭제
      fcmTokenUtil.deleteFcmToken(fcmToken);
      throw new BadRequestException(e.getMessage());
    }

    // 메세지 만들기
    Message message = makeMessage(fcmMessageToTargetDto);

    // 메세지 전송
    try {
      String response = FirebaseMessaging.getInstance().send(message);
      log.info("알림 전송 성공 {}", response);
    } catch (FirebaseMessagingException e) {
      log.error("알림 전송 실패 {}: {}", e.getErrorCode(), e.getMessage());
      fcmTokenUtil.deleteFcmToken(fcmToken);
      throw new RuntimeException(e);
    }
  }

  private MulticastMessage makeMulticastMessage(List<String> tokenList,
      FcmMessageDto fcmMessageDto) {

    return MulticastMessage.builder()
        // 수신 측 토큰 정보 - token
        .addAllTokens(tokenList)
        // 공통 알림 정보 - notification
        .setNotification(
            Notification.builder()
                .setTitle(fcmMessageDto.getTitle())
                .setBody(fcmMessageDto.getContent())
                .build())
        // Android 전용 설정 - android
        .setAndroidConfig(
            AndroidConfig.builder()
                .setNotification(
                    AndroidNotification.builder()
                        // click 이벤트 등 추가 가능
                        .setClickAction(fcmMessageDto.getClickAction())
                        .build()
                ).build())
        // IOS 전용 설정 - apns
        .setApnsConfig(
            ApnsConfig.builder()
                .setAps(
                    Aps.builder()
                        .setCategory(fcmMessageDto.getClickAction())
                        .build()
                ).build())
        .build();
  }

  private Message makeMessage(FcmMessageToTargetDto fcmMessageToTargetDto) {

    return Message.builder()
        // 수신 측 토큰 정보 - token
        .setToken(fcmMessageToTargetDto.getTargetToken())
        // 주제 - topic
        .setTopic("topic")
        // 공통 알림 정보 - notification
        .setNotification(
            Notification.builder()
                .setTitle(fcmMessageToTargetDto.getMessage().getTitle())
                .setBody(fcmMessageToTargetDto.getMessage().getContent())
                .build())
        // Android 전용 설정 - android
        .setAndroidConfig(
            AndroidConfig.builder()
                .setNotification(
                    AndroidNotification.builder()
                        // click 이벤트 등 추가 가능
                        .setClickAction(fcmMessageToTargetDto.getMessage().getClickAction())
                        .build()
                ).build())
        // IOS 전용 설정 - apns
        .setApnsConfig(
            ApnsConfig.builder()
                .setAps(
                    Aps.builder()
                        .setCategory(fcmMessageToTargetDto.getMessage().getClickAction())
                        .build()
                ).build())
        .build();
  }

  private List<List<String>> splitTokenList(List<String> tokenList) {
    List<List<String>> splitedTokenList = new ArrayList<>();
    int totalSize = tokenList.size();

    for (int i = 0; i < totalSize; i += 500) {
      List<String> chunk = new ArrayList<>(
          tokenList.subList(i, Math.min(totalSize, i + 500))
      );
      splitedTokenList.add(chunk);
    }

    return splitedTokenList;
  }
}
