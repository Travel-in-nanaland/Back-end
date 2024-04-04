package com.jeju.nanaland.global.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JwtResponse {

  private String accessToken;
  private String refreshToken;
}

