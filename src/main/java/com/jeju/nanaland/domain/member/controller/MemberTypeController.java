package com.jeju.nanaland.domain.member.controller;

import com.jeju.nanaland.domain.member.dto.MemberTypeRequest;
import com.jeju.nanaland.domain.member.service.MemberTypeService;
import com.jeju.nanaland.global.ApiResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberTypeController {

  private final MemberTypeService memberTypeService;

  @PatchMapping("/members/type")
  public ApiResponse<Null> updateMemberType(@RequestBody MemberTypeRequest request) {
    /**
     * memberId = 1 로 임시로 설정
     * 추후 AccessToken 형식 정해지면 update 필요
     */
    Long memberId = 1L;
    memberTypeService.updateMemberType(memberId, request.getType());
    return ApiResponse.success(SuccessCode.UPDATE_MEMBER_TYPE_SUCCESS);
  }
}
