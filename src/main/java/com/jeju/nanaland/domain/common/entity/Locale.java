package com.jeju.nanaland.domain.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

public enum Locale {
  KOREAN, ENGLISH, CHINESE, MALAYSIA;

  @JsonCreator
  public static Locale parsing(String inputValue) {
    return Stream.of(Locale.values())
        .filter(locale -> locale.toString().equals(inputValue.toUpperCase()))
        .findFirst()
        .orElse(null);
  }
}
