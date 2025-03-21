package com.jeju.nanaland.domain.member.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.GET_MEMBER_PROFILE_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.GET_POPULAR_POSTS_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.GET_RECOMMENDED_POSTS_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.JOIN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.LOGIN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.REISSUE_TOKEN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_LANGUAGE_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_MEMBER_CONSENT_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_MEMBER_PROFILE_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_MEMBER_TYPE_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.VALID_NICKNAME_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.WITHDRAWAL_SUCCESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.service.PostViewCountService;
import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberResponse;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.service.MemberConsentService;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.domain.member.service.MemberProfileService;
import com.jeju.nanaland.domain.member.service.MemberTypeService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import com.jeju.nanaland.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Tag(name = "회원(Member)", description = "회원(Member) API입니다.")
public class MemberController {

  private final MemberLoginService memberLoginService;
  private final MemberTypeService memberTypeService;
  private final MemberProfileService memberProfileService;
  private final MemberConsentService memberConsentService;
  private final PostViewCountService postViewCountService;

  @Operation(summary = "회원 가입", description = "회원 가입을 하면 JWT가 발급됩니다. ")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우, 필수 이용약관을 동의하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "회원 가입이 필요한 경우", content = @Content),
      @ApiResponse(responseCode = "409", description = "이미 가입된 계정이 있는 경우, 닉네임이 중복되는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @PostMapping(value = "/join")
  public BaseResponse<JwtDto> join(
      @RequestBody @Valid MemberRequest.JoinDto joinDto,
      @RequestParam(required = false) String fileKey) {
    JwtDto jwtDto = memberLoginService.join(joinDto, fileKey);
    return BaseResponse.success(JOIN_SUCCESS, jwtDto);
  }

  @Operation(summary = "로그인", description = "로그인을 하면 JWT가 발급됩니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "회원 가입이 필요한 경우", content = @Content)
  })
  @PostMapping("/login")
  public BaseResponse<JwtDto> login(@RequestBody @Valid MemberRequest.LoginDto loginDto) {
    JwtDto jwtDto = memberLoginService.login(loginDto);
    return BaseResponse.success(LOGIN_SUCCESS, jwtDto);
  }

  @Operation(summary = "로그아웃")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @PostMapping("/logout")
  public BaseResponse<Null> logout(@AuthMember MemberInfoDto memberInfoDto,
      @Parameter(name = "accessToken", hidden = true)
      @RequestHeader("Authorization") String accessToken,
      @RequestParam(required = false) String fcmToken) {
    memberLoginService.logout(memberInfoDto, accessToken, fcmToken);
    return BaseResponse.success(SuccessCode.LOGOUT_SUCCESS);
  }

  @Operation(summary = "JWT 재발급", description = "RefreshToken으로 AccessToken이 재발급됩니다."
      + "header에 AccessToken이 아닌 RefreshToken을 담아 요청해주세요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "존재하지 않는 회원인 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "RefreshToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/reissue")
  public BaseResponse<JwtDto> reissue(
      @Parameter(name = "refreshToken", hidden = true)
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken,
      @RequestParam(required = false) String fcmToken) {
    JwtDto jwtDto = memberLoginService.reissue(refreshToken, fcmToken);
    return BaseResponse.success(REISSUE_TOKEN_SUCCESS, jwtDto);
  }

  @Operation(
      summary = "테스트 결과에 따른 유저 타입 갱신",
      description = "결과 타입: GAMGYUL_ICECREAM, GAMGYUL_RICECAKE, GAMGYUL, GAMGYUL_CIDER, "
          + "GAMGYUL_AFFOKATO, GAMGYUL_HANGWA, GAMGYUL_JUICE, GAMGYUL_CHOCOLATE, GAMGYUL_COCKTAIL, "
          + "TANGERINE_PEEL_TEA, GAMGYUL_YOGURT, GAMGYUL_FLATCCINO, GAMGYUL_LATTE, GAMGYUL_SIKHYE, "
          + "GAMGYUL_ADE, GAMGYUL_BUBBLE_TEA")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "결과 타입에 없는 값으로 요청", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @PatchMapping("/type")
  public BaseResponse<Null> updateMemberType(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid MemberRequest.UpdateTypeDto updateTypeDto) {

    memberTypeService.updateMemberType(memberInfoDto, updateTypeDto);
    return BaseResponse.success(UPDATE_MEMBER_TYPE_SUCCESS);
  }

  @Operation(
      summary = "타입 테스트에 따른 추천 게시물 2개 반환",
      description =
          "https://docs.google.com/spreadsheets/d/1zhqBmQx6kGLWk3W29wVPDRSlQJqa8vfS0luX7enL_Cg/edit#gid=1427669947 "
              + "의 온보딩 페이지에 정리된 결과에 따른 추천 게시물 2개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "결과 타입에 없는 값으로 요청", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/recommended")
  public BaseResponse<List<MemberResponse.RecommendPostDto>> getRecommendPostsByType(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(required = false) Long memberId) {

    List<MemberResponse.RecommendPostDto> result = memberTypeService.getRecommendPostsByType(
        memberInfoDto, memberId);
    return BaseResponse.success(GET_RECOMMENDED_POSTS_SUCCESS, result);
  }

  @Operation(
      summary = "인기 게시물 조회",
      description =
          "지난주 조회수 높은 게시물 3개 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "결과 타입에 없는 값으로 요청", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/hot")
  public BaseResponse<List<PopularPostPreviewDto>> getPopularPosts(
      @AuthMember MemberInfoDto memberInfoDto) throws JsonProcessingException {

    List<PopularPostPreviewDto> result = postViewCountService.getLastWeekPopularPosts(
        memberInfoDto);
    return BaseResponse.success(GET_POPULAR_POSTS_SUCCESS, result);
  }

  @Operation(
      summary = "랜덤 추천 게시물 3개 반환",
      description = "홈에서 보여질 랜덤 추천 게시물 3개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "결과 타입에 없는 값으로 요청", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/recommended/random")
  public BaseResponse<List<MemberResponse.RecommendPostDto>> getRandomRecommendedPosts(
      @AuthMember MemberInfoDto memberInfoDto) {

    List<MemberResponse.RecommendPostDto> result = memberTypeService.getRandomRecommendedPosts(
        memberInfoDto);
    return BaseResponse.success(GET_RECOMMENDED_POSTS_SUCCESS, result);
  }

  @Operation(
      summary = "회원 탈퇴")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @PostMapping("/withdrawal")
  public BaseResponse<Null> withdrawal(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid MemberRequest.WithdrawalDto withdrawalType) {
    memberLoginService.withdrawal(memberInfoDto, withdrawalType);
    return BaseResponse.success(WITHDRAWAL_SUCCESS);
  }

  @Operation(
      summary = "유저 프로필 수정",
      description = "유저 닉네임, 설명, 프로필 사진 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "파일키 형식이 맞지 않는 등 입력값이 올바르지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "이미지 업로드에 실패한 경우", content = @Content)
  })
  @PatchMapping(value = "/profile")
  public BaseResponse<String> updateProfile(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid MemberRequest.ProfileUpdateDto reqDto,
      @RequestParam(required = false) String fileKey) {
    memberProfileService.updateProfile(memberInfoDto, reqDto, fileKey);
    return BaseResponse.success(UPDATE_MEMBER_PROFILE_SUCCESS);
  }

  @Operation(
      summary = "유저 프로필 조회",
      description = "유저 이메일, provider, 프로필 썸네일 이미지, 닉네임, 설명, 해시태그 리스트 반환."
          + "id를 입력하지 않으면 내 프로필이 조회됩니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/profile")
  public BaseResponse<MemberResponse.ProfileDto> getMemberProfile(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(required = false) Long id) {

    MemberResponse.ProfileDto profileDto = memberProfileService.getMemberProfile(memberInfoDto, id);
    return BaseResponse.success(GET_MEMBER_PROFILE_SUCCESS, profileDto);
  }

  @Operation(
      summary = "언어 설정 변경")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @PostMapping("/language")
  public BaseResponse<Null> updateLanguage(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid MemberRequest.LanguageUpdateDto languageUpdateDto) {
    memberProfileService.updateLanguage(memberInfoDto, languageUpdateDto);
    return BaseResponse.success(UPDATE_LANGUAGE_SUCCESS);
  }

  @Operation(
      summary = "이용약관 동의 여부 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @PostMapping("/consent")
  public BaseResponse<Boolean> updateMemberConsent(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid MemberRequest.ConsentUpdateDto consentUpdateDto
  ) {
    boolean consent = memberConsentService.updateMemberConsent(memberInfoDto, consentUpdateDto);
    return BaseResponse.success(UPDATE_MEMBER_CONSENT_SUCCESS, consent);
  }

  @Operation(
      summary = "강제 회원 탈퇴 [테스트용]", description = "[회원 탈퇴]를 먼저 진행해야 합니다. 탈퇴일을 3개월 전으로 업데이트 및 개인 정보 삭제하여 즉시 회원 탈퇴 처리")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @PostMapping("/forceWithdrawal")
  public BaseResponse<Null> forceWithdrawal(
      @Parameter(name = "accessToken", hidden = true)
      @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken
  ) {
    memberLoginService.forceWithdrawal(accessToken);
    return BaseResponse.success(WITHDRAWAL_SUCCESS);
  }

  @Operation(
      summary = "닉네임 중복 확인")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없거나 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "409", description = "닉네임이 중복되는 경우", content = @Content)
  })
  @GetMapping("/validateNickname")
  public BaseResponse<Void> validateNickname(
      @Pattern(
          regexp = "^[a-zA-Z0-9\\uAC00-\\uD7AF\\u3131-\\u318E\\u4E00-\\u9FFF\\u00C0-\\u024F\\u1E00-\\u1EFF][a-zA-Z0-9\\uAC00-\\uD7AF\\u3131-\\u318E\\u4E00-\\u9FFF\\u00C0-\\u024F\\u1E00-\\u1EFF ]{0,10}[a-zA-Z0-9\\uAC00-\\uD7AF\\u3131-\\u318E\\u4E00-\\u9FFF\\u00C0-\\u024F\\u1E00-\\u1EFF]$",
          message = "닉네임 형식이 올바르지 않습니다.")
      @NotBlank
      @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다")
      @RequestParam String nickname,
      @RequestParam(required = false) Long memberId) {
    if (memberId == null) {
      memberLoginService.validateNickname(nickname);
    } else {
      memberProfileService.validateNickname(nickname, memberId);
    }
    return BaseResponse.success(VALID_NICKNAME_SUCCESS);
  }
}
