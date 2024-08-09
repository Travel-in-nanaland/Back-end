package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NanalandNanalandNotificationRepository extends
    JpaRepository<NanalandNotification, Long>,
    NanalandNotificationRepositoryCustom {

}
