package com.jeju.nanaland.domain.member.controller;

import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.global.ApiResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
import com.jeju.nanaland.global.jwt.dto.JwtResponseDto.JwtDto;
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
public class MemberController {

  private final MemberLoginService memberLoginService;

  @PostMapping("/login")
  public ApiResponse<JwtDto> login(@RequestBody @Valid LoginDto loginDto) {
    JwtDto jwtDto = memberLoginService.login(loginDto);
    return ApiResponse.success(SuccessCode.LOGIN_SUCCESS, jwtDto);
  }

  @GetMapping("/reissue")
  public ApiResponse<String> reissue(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
    String newAccessToken = memberLoginService.reissue(refreshToken);
    return ApiResponse.success(SuccessCode.ACCESS_TOKEN_SUCCESS, newAccessToken);
  }
}

