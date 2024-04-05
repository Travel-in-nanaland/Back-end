package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.member.entity.MemberType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class MemberRequestDto {

  @Data
  public static class UpdateType {

    @NotBlank
    @EnumValid(
        enumClass = MemberType.class,
        message = "테스트 결과 타입이 유효하지 않습니다."
    )
    private String type;

    @NotNull
    private String test;
  }
}
