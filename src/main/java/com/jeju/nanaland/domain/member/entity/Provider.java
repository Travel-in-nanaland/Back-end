package com.jeju.nanaland.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum Provider {
  KAKAO, GOOGLE;

  @JsonCreator
  public static Provider parsing(String inputValue) {
    return Stream.of(Provider.values())
        .filter(provider -> provider.toString().equals(inputValue.toUpperCase()))
        .findFirst()
        .orElse(null);
  }
}
