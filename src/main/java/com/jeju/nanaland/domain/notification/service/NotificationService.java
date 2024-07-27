package com.jeju.nanaland.domain.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.FcmMessage;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final FcmTokenRepository fcmTokenRepository;
  private final String SENDER = "Nanaland";

  public String sendPushNotification(FcmMessage fcmMessage) {

    // FCM 토큰 조회
//    FcmToken fcmToken = fcmTokenRepository.findFcmTokenById(fcmMessage.getTargetToken())
//        .orElseThrow(() -> new NotFoundException("해당하는 FCM 토큰이 없습니다."));

    // 메세지 만들기
    Message message = makeMessage(fcmMessage);

    // 메세지 전송
    try {
      return FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException exception) {
      log.error("Fcm 메세지 전송 실패: {}", exception.getMessage());
      throw new RuntimeException(exception);
    }
  }

  private Message makeMessage(FcmMessage fcmMessage) {

    return Message.builder()
        .setToken(fcmMessage.getTargetToken())
        .setNotification(Notification.builder()
            .setTitle(fcmMessage.getTitle())
            .setBody(fcmMessage.getContent())
            .build())
        .setAndroidConfig(
            AndroidConfig.builder()
                .setNotification(
                    AndroidNotification.builder()
                        .setTitle(fcmMessage.getTitle())
                        .setBody(fcmMessage.getContent())
                        // click 이벤트
                        .build()
                ).build()
        ).build();
  }
}
