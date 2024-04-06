package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.dto.response.ImageFileResponse;
import com.jeju.nanaland.domain.common.dto.response.LanguageResponse;
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
    private LanguageResponse languageResponse;
    private ImageFileResponse imageFileResponse;
    private Long memberId;
    private String email;
    private String nickname;
    private String description;
  }
}
