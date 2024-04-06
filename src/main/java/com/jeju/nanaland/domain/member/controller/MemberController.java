package com.jeju.nanaland.domain.member.controller;

import com.jeju.nanaland.domain.member.dto.request.LoginRequest;
import com.jeju.nanaland.domain.member.dto.response.LoginResponse;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.global.ApiResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

  private final MemberLoginService memberLoginService;

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
    LoginResponse loginResponse = memberLoginService.login(loginRequest);
    return ApiResponse.success(SuccessCode.LOGIN_SUCCESS, loginResponse);
  }
}
