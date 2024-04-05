package com.jeju.nanaland.domain.member.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.GET_RECOMMENDED_POSTS_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.UPDATE_MEMBER_TYPE_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberRequestDto;
import com.jeju.nanaland.domain.member.dto.MemberResponseDto.RecommendedPosts;
import com.jeju.nanaland.domain.member.service.MemberTypeService;
import com.jeju.nanaland.global.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Slf4j
public class MemberController {

  private final MemberTypeService memberTypeService;

  @PatchMapping("/type")
  public ApiResponse<Null> updateMemberType(
      @RequestBody @Valid MemberRequestDto.UpdateType request) {
    /**
     * memberId = 1 로 임시로 설정
     * TODO: AccessToken 연동
     */
    Long memberId = 1L;
    memberTypeService.updateMemberType(memberId, request.getType());
    return ApiResponse.success(UPDATE_MEMBER_TYPE_SUCCESS);
  }

  @GetMapping("/recommended")
  public ApiResponse<List<RecommendedPosts>> getRecommendedPosts() {
    /**
     * memberId = 1 로 임시로 설정
     * TODO: AccessToken 연동
     */
    Long memberId = 1L;
    List<RecommendedPosts> result = memberTypeService.getRecommendedPostsByType(memberId);
    return ApiResponse.success(GET_RECOMMENDED_POSTS_SUCCESS, result);
  }
}
