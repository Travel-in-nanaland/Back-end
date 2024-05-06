package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.ConsentType;
import com.jeju.nanaland.domain.member.entity.MemberType;
import com.jeju.nanaland.domain.member.entity.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.Getter;

public class MemberRequest {

  @Getter
  @Schema(description = "로그인 요청 DTO")
  public static class LoginDto {

    @Schema(description = "언어", example = "KOREAN",
        allowableValues = {"KOREAN", "ENGLISH", "CHINESE", "MALAYSIA"})
    @NotNull
    @EnumValid(
        enumClass = Locale.class,
        message = "Locale이 유효하지 않습니다."
    )
    private String locale;

    @Schema(description = "이메일", example = "ABD123@kakao.com")
    @NotBlank
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Schema(description = "성별", example = "male", allowableValues = {"male", "female"})
    private String gender;

    @Schema(description = "생년월일", example = "2000-01-01")
    private LocalDate birthDate;

    @Schema(description = "소셜 로그인 Provider", example = "KAKAO",
        allowableValues = {"KAKAO", "GOOGLE", "APPLE", "GUEST"})
    @NotNull
    @EnumValid(
        enumClass = Provider.class,
        message = "Provider이 유효하지 않습니다."
    )
    private String provider;

    @Schema(description = "소셜 로그인 Provider ID", example = "1234567890")
    @NotNull
    private Long providerId;
  }

  @Data
  @Schema(description = "이용약관 동의 요청 DTO")
  public static class MemberConsentDTO {

    @Schema(description = "이용약관 동의 여부")
    @Valid
    List<ConsentItem> consentItems;
  }

  @Getter
  public static class ConsentItem {

    @Schema(description = "이용약관", example = "TERMS_OF_USE",
        allowableValues = {"TERMS_OF_USE", "MARKETING", "LOCATION_SERVICE"})
    @NotNull
    @EnumValid(
        enumClass = ConsentType.class,
        message = "ConsentType이 유효하지 않습니다."
    )
    private String consentType;

    @Schema(description = "동의 여부", defaultValue = "false")
    @NotNull
    private Boolean consent;
  }

  @Data
  @Schema(description = "타입 갱신 요청 DTO")
  public static class UpdateTypeDto {

    @NotBlank
    @EnumValid(
        enumClass = MemberType.class,
        message = "테스트 결과 타입이 유효하지 않습니다."
    )
    @Schema(
        description = "유저 타입",
        example = "GAMGYUL_ICECREAM",
        allowableValues = {"GAMGYUL_ICECREAM", "GAMGYUL_RICECAKE", "GAMGYUL", "GAMGYUL_CIDER",
            "GAMGYUL_AFFOKATO", "GAMGYUL_HANGWA", "GAMGYUL_JUICE", "GAMGYUL_CHOCOLATE",
            "GAMGYUL_COCKTAIL", "TANGERINE_PEEL_TEA", "GAMGYUL_YOGURT", "GAMGYUL_FLATCCINO",
            "GAMGYUL_LATTE", "GAMGYUL_SIKHYE", "GAMGYUL_ADE", "GAMGYUL_BUBBLE_TEA"})
    private String type;
  }
}
