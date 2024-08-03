package com.jeju.nanaland.domain.notification.service;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.jeju.nanaland.domain.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;

  @Transactional
  public void deleteFcmToken(FcmToken fcmToken) {
    fcmTokenRepository.delete(fcmToken);
  }

  public void verifyFcmToken(FcmToken fcmToken) throws FirebaseException {
    FirebaseAuth.getInstance().verifyIdToken(fcmToken.getToken());
  }
}
