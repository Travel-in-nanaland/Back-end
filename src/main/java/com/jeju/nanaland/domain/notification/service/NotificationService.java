package com.jeju.nanaland.domain.notification.service;

import static com.jeju.nanaland.global.exception.ErrorCode.NOTIFICATION_FORBIDDEN;

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
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notification.data.MemberNotificationCompose;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationWithTargetDto;
import com.jeju.nanaland.domain.notification.data.NotificationResponse;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationDetailDto;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationListDto;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import com.jeju.nanaland.domain.notification.repository.NanalandNanalandNotificationRepository;
import com.jeju.nanaland.domain.notification.util.FcmTokenUtil;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ForbiddenException;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final FcmTokenUtil fcmTokenUtil;
  private final NanalandNanalandNotificationRepository nanalandNotificationRepository;
  private final FcmTokenRepository fcmTokenRepository;
  private final FavoriteRepository favoriteRepository;

  public NotificationResponse.NotificationListDto getNotificationList(MemberInfoDto memberInfoDto,
      String fcmToken, int page, int size) {

    // 해당 토큰 조회
    FcmToken targetToken = fcmTokenRepository.findByToken(fcmToken)
        .orElseThrow(() -> new NotFoundException("해당 토큰 정보가 없습니다."));

    // 토큰 정보의 유저와 동일한지 검사
    if (targetToken.getMember() != memberInfoDto.getMember()) {
      throw new ForbiddenException(NOTIFICATION_FORBIDDEN.getMessage());
    }

    // 알림 리스트 조회
    Pageable pageable = PageRequest.of(page, size);
    Page<NanalandNotification> resultPage =
        nanalandNotificationRepository.findAllNotificationByMember(memberInfoDto.getMember(),
            pageable);

    return NotificationListDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(resultPage.getContent()
            .stream()
            .map(notification -> NotificationDetailDto.builder()
                .notificationId(notification.getId())
                .clickEvent(notification.getClickEvent().name())
                .category(notification.getNotificationCategory().name())
                .contentId(notification.getContentId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .build()
            ).toList()
        ).build();
  }

  @Transactional
  public void sendPushNotificationToAllMembers(NotificationDto notificationDto) {

    Language language = notificationDto.getLanguage();

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
      MulticastMessage message = makeMulticastMessage(tokenList, notificationDto);

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
  public void sendPushNotificationToTarget(NotificationWithTargetDto notificationWithTargetDto) {

    // 타겟 토큰 조회
    String targetToken = notificationWithTargetDto.getTargetToken();
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

    // 알림 정보 저장
    NotificationDto notificationDto = notificationWithTargetDto.getNotificationDto();
    NanalandNotification nanalandNotification = saveNanalandNotification(notificationDto);

    // 메세지 만들기
    Message message = makeMessage(notificationWithTargetDto);

    // 메세지 전송
    try {
      String response = FirebaseMessaging.getInstance().send(message);
      log.info("알림 전송 성공 {}", response);
    } catch (FirebaseMessagingException e) {
      log.error("알림 전송 실패 {}: {}", e.getErrorCode(), e.getMessage());
      fcmTokenUtil.deleteFcmToken(fcmToken);
      throw new RuntimeException(e);
    }

    // 전송한 알림 정보를 유저와 매핑
  }

  // 매일 10시에 나의 찜 알림 대상에게 알림 전송
  @Transactional
  @Scheduled(cron = "0 0 10 * * *")
  protected void sendMyFavoriteNotification() {
    List<Favorite> allFavorites = favoriteRepository.findAll();

    // 한 달 이상 전에 생성되었고, 생성된 이후 같은 게시물에 대해 5개 이상의 좋아요가 있을 때
    List<Favorite> filteredFavorites = allFavorites.stream()
        .filter(favorite -> {
              LocalDateTime createdAt = favorite.getCreatedAt();
              Long postId = favorite.getPost().getId();

              return favorite.getCreatedAt().isBefore(LocalDateTime.now()) &&
                  countSamePostIdCreatedAfter(allFavorites, createdAt, postId) >= 5;
            }
        ).toList();

    // 알림id, memberId, contentCategory, contentId 모두 조회
    List<MemberNotificationCompose> memberNotificationComposes =
        nanalandNotificationRepository.findAllMemberNotificationCompose();

  }

  private NanalandNotification saveNanalandNotification(NotificationDto notificationDto) {
    NanalandNotification newNotification =
        NanalandNotification.buildNanalandNotification(notificationDto);
    return nanalandNotificationRepository.save(newNotification);
  }

  private MulticastMessage makeMulticastMessage(List<String> tokenList,
      NotificationDto notificationDto) {

    return MulticastMessage.builder()
        // 수신 측 토큰 정보 - token
        .addAllTokens(tokenList)
        // 알림 내용 정보 - data
        .putData("category", notificationDto.getCategory().name())
        .putData("contentId", notificationDto.getContentId().toString())
        // 공통 알림 정보 - notification
        .setNotification(
            Notification.builder()
                .setTitle(notificationDto.getTitle())
                .setBody(notificationDto.getContent())
                .build())
        // Android 전용 설정 - android
        .setAndroidConfig(
            AndroidConfig.builder()
                .setNotification(
                    AndroidNotification.builder()
                        .build()
                ).build())
        // IOS 전용 설정 - apns
        .setApnsConfig(
            ApnsConfig.builder()
                .setAps(
                    Aps.builder()
                        .build()
                ).build())
        .build();
  }

  private Message makeMessage(NotificationWithTargetDto notificationWithTargetDto) {

    NotificationDto notificationDto = notificationWithTargetDto.getNotificationDto();

    return Message.builder()
        // 수신 측 토큰 정보 - token
        .setToken(notificationWithTargetDto.getTargetToken())
        // 알림 내용 정보 - data
        .putData("category", notificationDto.getCategory().name())
        .putData("contentId", notificationDto.toString())
        // 공통 알림 정보 - notification
        .setNotification(
            Notification.builder()
                .setTitle(notificationDto.getTitle())
                .setBody(notificationDto.getContent())
                .build())
        // Android 전용 설정 - android
        .setAndroidConfig(
            AndroidConfig.builder()
                .setNotification(
                    AndroidNotification.builder()
                        .build()
                ).build())
        // IOS 전용 설정 - apns
        .setApnsConfig(
            ApnsConfig.builder()
                .setAps(
                    Aps.builder()
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

  private long countSamePostIdCreatedAfter(List<Favorite> favoriteList, LocalDateTime localDateTime,
      Long postId) {
    return favoriteList.stream()
        .filter(favorite -> favorite.getPost().getId().equals(postId) &&
            favorite.getCreatedAt().isAfter(localDateTime))
        .toList().size();
  }
}
