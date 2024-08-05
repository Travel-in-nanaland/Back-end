package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
    NotificationRepositoryCustom {

}
