package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.entity.Locale;

public class PostService {

  public static String extractAddressTag(Locale locale, String address) {
    String result = "";

    switch (locale) {
      case KOREAN -> {
        if (address.contains("제주시")) {
          result = "제주시";
        } else {
          result = "서귀포시";
        }
      }

      case ENGLISH, MALAYSIA -> {
        if (address.contains("Jeju-si")) {
          result = "Jeju-si";
        } else {
          result = "Seogwipo-si";
        }
      }
      case CHINESE -> {
        if (address.contains("济州市")) {
          result = "济州市";
        } else {
          result = "西归浦市";
        }
      }
    }

    return result;
  }
}
