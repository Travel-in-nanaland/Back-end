package com.jeju.nanaland.domain.notification.util;

import com.jeju.nanaland.domain.common.data.Language;
import org.springframework.stereotype.Component;

@Component
public class FavoriteNotificationUtil {

  public String getNotificationTitle(String postTitle, Language language) {
    return switch (language) {
      case KOREAN -> "[나의 찜] " + postTitle;
      case ENGLISH -> "[My Favorite] " + postTitle;
      case CHINESE -> "[My Favorite] " + postTitle;
      case MALAYSIA -> "[My Favorite] " + postTitle;
      case VIETNAMESE -> "[My Favorite] " + postTitle;
    };
  }
}
