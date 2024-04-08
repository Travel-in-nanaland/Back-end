package com.jeju.nanaland.global.jwt.dto;

import lombok.Builder;
import lombok.Getter;

public class JwtResponseDto {

  private JwtResponseDto() {
  }

  @Builder
  @Getter
  public static class JwtDto {

    private String accessToken;
    private String refreshToken;
  }
}
