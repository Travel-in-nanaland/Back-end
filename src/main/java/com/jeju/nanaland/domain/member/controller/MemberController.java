package com.jeju.nanaland.domain.member.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.ACCESS_TOKEN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.GET_RECOMMENDED_POSTS_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.LOGIN_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_MEMBER_TYPE_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberRequest;
import com.jeju.nanaland.domain.member.dto.MemberRequest.LoginDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.RecommendPostDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.service.MemberLoginService;
import com.jeju.nanaland.domain.member.service.MemberTypeService;
import com.jeju.nanaland.global.ApiResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import com.jeju.nanaland.global.jwt.dto.JwtResponseDto.JwtDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

  private final MemberLoginService memberLoginService;
  private final MemberTypeService memberTypeService;


  @PostMapping("/login")
  public ApiResponse<JwtDto> login(@RequestBody @Valid LoginDto loginDto) {
    JwtDto jwtDto = memberLoginService.login(loginDto);
    return ApiResponse.success(LOGIN_SUCCESS, jwtDto);
  }

  @GetMapping("/reissue")
  public ApiResponse<String> reissue(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String refreshToken) {
    String newAccessToken = memberLoginService.reissue(refreshToken);
    return ApiResponse.success(ACCESS_TOKEN_SUCCESS, newAccessToken);
  }

  @PatchMapping("/type")
  public ApiResponse<Null> updateMemberType(
      @AuthMember Member member,
      @RequestBody @Valid MemberRequest.UpdateTypeDto request) {

    memberTypeService.updateMemberType(member.getId(), request.getType());
    return ApiResponse.success(UPDATE_MEMBER_TYPE_SUCCESS);
  }

  @GetMapping("/recommended")
  public ApiResponse<List<RecommendPostDto>> getRecommendedPosts(
      @AuthMember Member member) {

    List<RecommendPostDto> result = memberTypeService.getRecommendPostsByType(member.getId());
    return ApiResponse.success(GET_RECOMMENDED_POSTS_SUCCESS, result);
  }
}
