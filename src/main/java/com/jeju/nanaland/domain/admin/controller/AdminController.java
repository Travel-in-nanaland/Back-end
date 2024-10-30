package com.jeju.nanaland.domain.admin.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.LOGIN_SUCCESS;

import com.jeju.nanaland.domain.admin.service.AdminLoginService;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.jwt.dto.JwtResponseDto.JwtDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

  private final AdminLoginService adminLoginService;

  @GetMapping("")
  public String home() {
    return "admin/admin-home.html";
  }

  @GetMapping("/login")
  public String loginPage(Model model) {
    model.addAttribute("loginDto", new LoginDto());
    return "admin/admin-login.html";
  }

  @ResponseBody
  @PostMapping("/login")
  public BaseResponse<JwtDto> requestLogin(@RequestBody @Valid LoginDto loginDto) {
    JwtDto jwtDto = adminLoginService.adminLogin(loginDto);
    return BaseResponse.success(LOGIN_SUCCESS, jwtDto);
  }
}
