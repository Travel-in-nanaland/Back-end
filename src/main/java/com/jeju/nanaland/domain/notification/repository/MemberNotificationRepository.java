package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.notification.entity.MemberNotification;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {

  Optional<MemberNotification> findByMemberIdAndNanalandNotification(Long memberId,
      NanalandNotification nanalandNotification);
}
