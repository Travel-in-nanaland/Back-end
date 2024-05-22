package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.WithdrawalType;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.Getter;

public class MemberRequest {

  @Data
  @Schema(description = "회원 가입 요청 DTO")
  public static class JoinDto {

    @Schema(description = "이용약관 동의 여부")
    @Valid
    List<ConsentItem> consentItems;

    @Schema(description = "이메일(필수) - GUEST이면 GUEST@nanaland.com로 임시 지정하여 요청", example = "ABD123@kakao.com")
    @NotBlank
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Schema(description = "소셜 로그인 Provider(필수)", example = "KAKAO",
        allowableValues = {"KAKAO", "GOOGLE", "APPLE", "GUEST"})
    @NotNull
    @EnumValid(
        enumClass = Provider.class,
        message = "Provider이 유효하지 않습니다."
    )
    private String provider;

    @Schema(description = "소셜 로그인 Provider ID(필수) - GUEST이면 디바이스 ID", example = "1234567890")
    @NotBlank
    private String providerId;

    @Schema(description = "언어(필수)", example = "KOREAN",
        allowableValues = {"KOREAN", "ENGLISH", "CHINESE", "MALAYSIA"})
    @NotNull
    @EnumValid(
        enumClass = Locale.class,
        message = "Locale이 유효하지 않습니다."
    )
    private String locale;

    @Schema(description = "성별", example = "male", allowableValues = {"male", "female"})
    private String gender;

    @Schema(description = "생년월일", example = "2000-01-01")
    private LocalDate birthDate;

    @Schema(description = "닉네임(필수) - GUEST이면 GUEST로 임시 지정하여 요청")
    @NotBlank
    @Size(max = 12, message = "닉네임 최대 길이 초과")
    private String nickname;
  }

  @Data
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

    @Schema(description = "소셜 로그인 Provider", example = "KAKAO",
        allowableValues = {"KAKAO", "GOOGLE", "APPLE", "GUEST"})
    @NotNull
    @EnumValid(
        enumClass = Provider.class,
        message = "Provider이 유효하지 않습니다."
    )
    private String provider;

    @Schema(description = "소셜 로그인 Provider ID", example = "1234567890")
    @NotBlank
    private String providerId;
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

  @Getter
  public static class ConsentUpdateDto {

    @Schema(description = "이용약관", example = "TERMS_OF_USE",
        allowableValues = {"MARKETING", "LOCATION_SERVICE"})
    @NotNull
    @EnumValid(
        enumClass = ConsentType.class,
        exclude = "TERMS_OF_USE",
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
        enumClass = TravelType.class,
        exclude = "NONE",
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

  @Data
  @Schema(description = "회원 탈퇴 요청 DTO")
  public static class WithdrawalDto {

    @Schema(description = "탈퇴 사유", example = "INSUFFICIENT_CONTENT",
        allowableValues = {"INSUFFICIENT_CONTENT", "INCONVENIENT_SERVICE", "INCONVENIENT_COMMUNITY",
            "RARE_VISITS"})
    @NotBlank
    @EnumValid(
        enumClass = WithdrawalType.class,
        message = "WithdrawalType이 유효하지 않습니다."
    )
    private String withdrawalType;

  }


  @Data
  @Schema(description = "프로필 정보 업데이트 요청 DTO")
  public static class ProfileUpdateDto {

    @Schema(description = "닉네임")
    @NotBlank
    @Size(max = 12, message = "닉네임 최대 길이 초과")
    private String nickname;

    @Schema(description = "소개")
    @Size(max = 70, message = "소개 최대 길이 초과")
    private String description;
  }

  @Data
  @Schema(description = "언어 설정 변경 요청 DTO")
  public static class LanguageUpdateDto {

    @Schema(description = "언어", example = "KOREAN",
        allowableValues = {"KOREAN", "ENGLISH", "CHINESE", "MALAYSIA"})
    @NotNull
    @EnumValid(
        enumClass = Locale.class,
        message = "Locale이 유효하지 않습니다."
    )
    private String locale;
  }
}
