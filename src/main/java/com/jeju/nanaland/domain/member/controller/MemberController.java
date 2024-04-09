package com.jeju.nanaland.domain.member.controller;

import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
import com.jeju.nanaland.global.jwt.dto.JwtResponseDto.JwtDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Tag(name = "회원(Member) 컨트롤러", description = "회원(Member) API입니다.")
public class MemberController {

  private final MemberLoginService memberLoginService;

  @Operation(summary = "로그인", description = "로그인을 하면 JWT가 발급됩니다.")
  @PostMapping("/login")
  public BaseResponse<JwtDto> login(@RequestBody @Valid LoginDto loginDto) {
    JwtDto jwtDto = memberLoginService.login(loginDto);
    return BaseResponse.success(SuccessCode.LOGIN_SUCCESS, jwtDto);
  }

  @Operation(summary = "AccessToken 재발급", description = "RefreshToken으로 AccessToken이 재발급됩니다.")
  @GetMapping("/reissue")
  public BaseResponse<String> reissue(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
    String newAccessToken = memberLoginService.reissue(refreshToken);
    return BaseResponse.success(SuccessCode.ACCESS_TOKEN_SUCCESS, newAccessToken);
  }
}

