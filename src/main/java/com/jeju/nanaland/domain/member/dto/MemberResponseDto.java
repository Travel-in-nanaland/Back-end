package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.dto.response.ImageFileResponseDto;
import com.jeju.nanaland.domain.common.dto.response.LanguageResponseDto;
import com.jeju.nanaland.global.jwt.dto.JwtResponse;
import lombok.Builder;
import lombok.Getter;

public class MemberResponseDto {

  private MemberResponseDto() {
  }

  @Getter
  @Builder
  public static class LoginResponse {

    private JwtResponse jwtResponse;
    private LanguageResponseDto languageResponseDto;
    private ImageFileResponseDto imageFileResponseDto;
    private Long memberId;
    private String email;
    private String nickname;
    private String description;
  }
}
