package com.jeju.nanaland.domain.notification.util;

import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class FcmTokenUtil {

  private final FcmTokenRepository fcmTokenRepository;

  @Transactional
  public void deleteFcmToken(FcmToken fcmToken) {
    fcmTokenRepository.delete(fcmToken);
  }

  public boolean isFcmTokenExpired(FcmToken fcmToken) {
    LocalDateTime now = LocalDateTime.now();
    return fcmToken.getTimestamp().isBefore(now.minusMonths(2));
  }
}
