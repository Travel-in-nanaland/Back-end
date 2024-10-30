package com.jeju.nanaland.domain.notification.service;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;

  public FcmToken createFcmToken(Member member, String fcmToken) {
    return fcmTokenRepository.save(
        FcmToken.builder()
            .member(member)
            .token(fcmToken)
            .timestamp(LocalDateTime.now())
            .build()
    );
  }

  public FcmToken getFcmToken(Member member, String fcmToken) {
    return fcmTokenRepository.findByMemberAndToken(member, fcmToken).orElse(null);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteFcmToken(FcmToken fcmToken) {
    fcmTokenRepository.delete(fcmToken);
  }

  public boolean isFcmTokenExpired(FcmToken fcmToken) {
    LocalDateTime now = LocalDateTime.now();
    return fcmToken.getTimestamp().isBefore(now.minusMonths(2));
  }
}
