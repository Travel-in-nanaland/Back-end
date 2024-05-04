package com.jeju.nanaland.domain.member.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.ACCESS_TOKEN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.GET_RECOMMENDED_POSTS_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.LOGIN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_MEMBER_TYPE_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.dto.MemberRequest.MemberConsentDTO;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.service.MemberConsentService;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.domain.member.service.MemberTypeService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import com.jeju.nanaland.global.jwt.dto.JwtResponseDto.JwtDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Tag(name = "회원(Member)", description = "회원(Member) API입니다.")
public class MemberController {

  private final MemberLoginService memberLoginService;
  private final MemberTypeService memberTypeService;
  private final MemberConsentService memberConsentService;


  @Operation(summary = "로그인", description = "로그인을 하면 JWT가 발급됩니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "409", description = "데이터가 중복되는 경우", content = @Content)
  })
  @PostMapping("/login")
  public BaseResponse<JwtDto> login(@RequestBody @Valid LoginDto loginDto) {
    JwtDto jwtDto = memberLoginService.login(loginDto);
    return BaseResponse.success(LOGIN_SUCCESS, jwtDto);
  }

  @Operation(summary = "AccessToken 재발급", description = "RefreshToken으로 AccessToken이 재발급됩니다."
      + "header에 AccessToken이 아닌 RefreshToken을 담아 요청해주세요.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "존재하지 않는 회원인 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "RefreshToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/reissue")
  public BaseResponse<String> reissue(
      @Parameter(name = "refreshToken", hidden = true)
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
    String newAccessToken = memberLoginService.reissue(refreshToken);
    return BaseResponse.success(ACCESS_TOKEN_SUCCESS, newAccessToken);
  }

  @PostMapping("/consent")
  public ResponseEntity updateConsent(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid MemberConsentDTO memberConsentDTO) {
    memberConsentService.updateConsent(memberInfoDto, memberConsentDTO);
    return ResponseEntity.ok().build();
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
      @RequestBody @Valid MemberRequest.UpdateTypeDto request) {

    memberTypeService.updateMemberType(memberInfoDto.getMember().getId(), request.getType());
    return BaseResponse.success(UPDATE_MEMBER_TYPE_SUCCESS);
  }

  @Operation(
      summary = "유저 타입에 따른 추천 게시물 2개 반환",
      description =
          "https://docs.google.com/spreadsheets/d/1zhqBmQx6kGLWk3W29wVPDRSlQJqa8vfS0luX7enL_Cg/edit#gid=1427669947 "
              + "의 온보딩 페이지에 정리된 결과에 따른 추천 게시물 2개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "결과 타입에 없는 값으로 요청", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/recommended")
  public BaseResponse<List<RecommendPostDto>> getRecommendedPosts(
      @AuthMember MemberInfoDto memberInfoDto) {

    List<RecommendPostDto> result = memberTypeService.getRecommendPostsByType(
        memberInfoDto.getMember().getId());
    return BaseResponse.success(GET_RECOMMENDED_POSTS_SUCCESS, result);
  }
}
