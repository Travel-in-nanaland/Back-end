package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.entity.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.Getter;


public class MemberRequest {

  private MemberRequest() {
  }

  @Getter
  public static class LoginDto {

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

  @Data
  public static class UpdateTypeDto {

    @NotBlank
    @EnumValid(
        enumClass = MemberType.class,
        message = "테스트 결과 타입이 유효하지 않습니다."
    )
    private String type;
  }
}
