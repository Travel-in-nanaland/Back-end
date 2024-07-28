package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.notification.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByToken(String token);
}
