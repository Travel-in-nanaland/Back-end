package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.notification.entity.FavoriteNotification;
import java.util.List;

public interface FavoriteNotificationRepositoryCustom {

  List<FavoriteNotification> findAllFavoriteNotificationToSend();
}
