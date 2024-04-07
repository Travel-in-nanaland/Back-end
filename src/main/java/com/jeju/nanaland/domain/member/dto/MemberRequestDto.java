package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;


public class MemberRequestDto {

  private MemberRequestDto() {
  }

  @Getter
  public static class LoginRequest {

    @NotNull
    private Locale locale;

    @NotBlank
    private String email;

    private String gender;

    private LocalDate birthDate;

    @NotNull
    private Provider provider;

    @NotNull
    private Long providerId;
  }
}
