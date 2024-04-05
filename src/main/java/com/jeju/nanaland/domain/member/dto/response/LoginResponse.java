package com.jeju.nanaland.domain.member.dto.response;

import com.jeju.nanaland.domain.common.dto.response.ImageFileResponse;
import com.jeju.nanaland.domain.common.dto.response.LanguageResponse;
import com.jeju.nanaland.global.jwt.dto.JwtResponse;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse {

  private JwtResponse jwtResponse;
  private LanguageResponse languageResponse;
  private ImageFileResponse imageFileResponse;
  private String email;
  private String nickname;
  private String description;
}
