package com.jeju.nanaland.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.repository.MemberConsentRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationDto;
import com.jeju.nanaland.domain.notification.data.NotificationRequest.NotificationWithTargetDto;
import com.jeju.nanaland.domain.notification.data.NotificationResponse;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationDetailDto;
import com.jeju.nanaland.domain.notification.data.NotificationResponse.NotificationListDto;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.entity.MemberNotification;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import com.jeju.nanaland.domain.notification.repository.MemberNotificationRepository;
import com.jeju.nanaland.domain.notification.repository.NanalandNotificationRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  private final FcmTokenService fcmTokenService;
  private final MemberRepository memberRepository;
  private final NanalandNotificationRepository nanalandNotificationRepository;
  private final MemberNotificationRepository memberNotificationRepository;
  private final MemberConsentRepository memberConsentRepository;
  private final FcmTokenRepository fcmTokenRepository;

  public NotificationResponse.NotificationListDto getNotificationList(MemberInfoDto memberInfoDto,
      int page, int size) {

    // 회원과 연결된 알림 리스트 조회
    Pageable pageable = PageRequest.of(page, size);
    Page<NanalandNotification> resultPage =
        nanalandNotificationRepository.findAllNotificationByMember(memberInfoDto.getMember(),
            pageable);

    List<NotificationDetailDto> data = resultPage.stream()
        .map(notification -> NotificationDetailDto.builder()
            .notificationId(notification.getId())
            .clickEvent(notification.getNotificationCategory().getClickEvent().name())
            .category(notification.getNotificationCategory().name())
            .contentId(notification.getContentId())
            .title(notification.getTitle())
            .content(notification.getContent())
            .createdAt(notification.getCreatedAt())
            .build()
        ).toList();

    return NotificationListDto.builder()
        .totalElements(resultPage.getTotalElements())
        .data(data)
        .build();
  }

  @Transactional
  public void sendPushNotificationToAll(NotificationDto notificationDto) {

    Language language = notificationDto.getLanguage();

    // 동일한 알림 정보가 있다면 가져오고 없다면 생성
    NanalandNotification nanalandNotification = getNanalandNotification(notificationDto);
    if (nanalandNotification == null) {
      nanalandNotification = saveNanalandNotification(notificationDto);
    }

    // 전송한 알림 정보를 유저와 매핑
    List<Member> members = memberRepository.findAll();
    for (Member member : members) {
      // 전송한 알림이 매핑이 안되어 있다면 추가
      if (getMemberNotification(member, nanalandNotification) == null) {
        saveMemberNotification(member, nanalandNotification);
      }
    }

    // 알림 전송 요청 언어를 사용하는 모든 토큰 조회, 검증
    List<FcmToken> validTokenList = fcmTokenRepository.findAllByMemberLanguage(language)
        .stream()
        .filter(fcmToken -> {
          if (!fcmTokenService.isFcmTokenExpired(fcmToken)) {
            return true;
          } else {
            // 검증에 실패한 토큰 삭제
            fcmTokenService.deleteFcmToken(fcmToken);
            log.info("Invalid fcm token deleted: {}", fcmToken.getToken());
            return false;
          }
        })
        .toList();

    // 한번에 전송 가능한 단위인 500개로 분할
    List<List<FcmToken>> splitedTokenList = splitTokenList(validTokenList);

    int currentSuccessCount = 0;
    for (List<FcmToken> tokenList : splitedTokenList) {
      // NOTIFICATION 동의가 있는 경우에만 전송
      tokenList = tokenList.stream()
          .filter(token -> {
            Member member = token.getMember();
            Optional<MemberConsent> memberConsentOptional = memberConsentRepository
                .findByConsentTypeAndMember(ConsentType.NOTIFICATION, member);
            return memberConsentOptional.isPresent();
          })
          .toList();

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
      // ApiFuture 에러 처리, 쓰레드 인터럽트 에러 처리
      catch (ExecutionException | InterruptedException e) {
        log.error("알림 전송 실패: {}", e.getMessage());
        throw new RuntimeException(e);
      }

    }
  }

  @Transactional
  public void sendPushNotificationToSingleTarget(
      NotificationWithTargetDto notificationWithTargetDto) {

    // 타겟 토큰 조회
    Long memberId = notificationWithTargetDto.getMemberId();
    Member member = memberRepository.findMemberById(memberId)
        .orElseThrow(() -> new NotFoundException("해당 유저가 없습니다. memberId: " + memberId));

    // 동일한 알림 정보가 있다면 가져오고 없다면 생성
    NotificationDto notificationDto = notificationWithTargetDto.getNotificationDto();
    NanalandNotification nanalandNotification = getNanalandNotification(notificationDto);
    if (nanalandNotification == null) {
      nanalandNotification = saveNanalandNotification(notificationDto);
    }

    // 전송한 알림이 유저와 매핑이 안되어 있다면 추가
    if (getMemberNotification(member, nanalandNotification) == null) {
      saveMemberNotification(member, nanalandNotification);
    }

    // 알림 동의 여부 조회
    Optional<MemberConsent> memberConsentOptional = memberConsentRepository.findByConsentTypeAndMember(
        ConsentType.NOTIFICATION, member);
    // 알림 동의를 하지 않은 유저라면 푸시알림을 보내지 않고 종료
    if (memberConsentOptional.isEmpty() || !memberConsentOptional.get().getConsent()) {
      return;
    }

    List<FcmToken> fcmTokenList = fcmTokenRepository.findAllByMember(member);
    // FCM 토큰 검증
    List<FcmToken> validTokenList = fcmTokenList
        .stream()
        .filter(fcmToken -> {
          if (!fcmTokenService.isFcmTokenExpired(fcmToken)) {
            return true;
          } else {
            // 검증에 실패한 토큰 삭제
            fcmTokenService.deleteFcmToken(fcmToken);
            log.info("Invalid fcm token deleted: {}", fcmToken.getToken());
            return false;
          }
        })
        .toList();
    if (fcmTokenList.isEmpty()) {
      throw new NotFoundException("해당 유저의 유효한 fcm 토큰 정보가 없습니다.");
    }

    // 메세지 만들기
    MulticastMessage message = makeMulticastMessage(validTokenList, notificationDto);

    // 메세지 전송
    try {
      ApiFuture<BatchResponse> responseApiFuture = FirebaseMessaging.getInstance()
          .sendEachForMulticastAsync(message);
      BatchResponse batchResponse = responseApiFuture.get();
      int successCount = batchResponse.getSuccessCount();
      log.info("알림 전송 성공: {}개", successCount);
    }
    // ApiFuture 에러 처리, 쓰레드 인터럽트 에러 처리
    catch (ExecutionException | InterruptedException e) {
      log.error("알림 전송 실패: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private MulticastMessage makeMulticastMessage(List<FcmToken> tokenList,
      NotificationDto notificationDto) {

    return MulticastMessage.builder()
        // 수신 측 토큰 정보 - token
        .addAllTokens(
            tokenList.stream()
                .map(FcmToken::getToken)
                .toList())
        // 알림 내용 정보 - data
        .putData("clickEvent", notificationDto.getCategory().getClickEvent().name())
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

  private List<List<FcmToken>> splitTokenList(List<FcmToken> tokenList) {

    List<List<FcmToken>> splitedTokenList = new ArrayList<>();
    int totalSize = tokenList.size();

    for (int i = 0; i < totalSize; i += 500) {
      List<FcmToken> chunk = new ArrayList<>(
          tokenList.subList(i, Math.min(totalSize, i + 500))
      );
      splitedTokenList.add(chunk);
    }

    return splitedTokenList;
  }

  private NanalandNotification getNanalandNotification(NotificationDto notificationDto) {

    Optional<NanalandNotification> result = nanalandNotificationRepository.findByNotificationInfo(
        notificationDto.getCategory(),
        notificationDto.getContentId(),
        notificationDto.getTitle(),
        notificationDto.getContent());

    return result.orElse(null);
  }

  private NanalandNotification saveNanalandNotification(NotificationDto notificationDto) {

    NanalandNotification newNotification =
        NanalandNotification.buildNanalandNotification(notificationDto);
    return nanalandNotificationRepository.save(newNotification);
  }

  private MemberNotification getMemberNotification(Member member,
      NanalandNotification nanalandNotification) {

    Long memberId = member.getId();
    Optional<MemberNotification> result = memberNotificationRepository
        .findByMemberIdAndNanalandNotification(memberId, nanalandNotification);

    return result.orElse(null);
  }

  private MemberNotification saveMemberNotification(Member member,
      NanalandNotification nanalandNotification) {

    MemberNotification memberNotification = MemberNotification.builder()
        .memberId(member.getId())
        .nanalandNotification(nanalandNotification)
        .build();
    return memberNotificationRepository.save(memberNotification);
  }
}
