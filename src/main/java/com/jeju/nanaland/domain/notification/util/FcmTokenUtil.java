package com.jeju.nanaland.domain.notification.util;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
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

  public void verifyFcmToken(FcmToken fcmToken) throws FirebaseException {
    FirebaseAuth.getInstance().verifyIdToken(fcmToken.getToken());
  }
}
