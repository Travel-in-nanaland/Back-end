package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long>,
    FcmTokenRepositoryCustom {

  Optional<FcmToken> findByMemberAndToken(Member member, String token);
}
