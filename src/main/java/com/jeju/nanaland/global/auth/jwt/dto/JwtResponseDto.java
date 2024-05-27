package com.jeju.nanaland.global.auth.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class JwtResponseDto {

  private JwtResponseDto() {
  }

  @Builder
  @Getter
  @Schema(description = "JWT 응답 DTO")
  public static class JwtDto {

    private String accessToken;
    private String refreshToken;
  }
}
