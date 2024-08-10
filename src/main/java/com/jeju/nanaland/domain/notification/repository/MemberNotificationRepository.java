package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.notification.entity.MemberNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {

}
