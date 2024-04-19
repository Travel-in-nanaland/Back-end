package com.jeju.nanaland.domain.experience.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.POST_LIKE_TOGGLE_SUCCESS;

import com.jeju.nanaland.domain.experience.service.ExperienceService;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/experience")
@RequiredArgsConstructor
@Slf4j
public class ExperienceController {

  private final ExperienceService experienceService;

  @PostMapping("/like/{id}")
  public BaseResponse<String> toggleLikeStatus(@AuthMember Member member, @PathVariable Long id) {
    String result = experienceService.toggleLikeStatus(member, id);
    return BaseResponse.success(POST_LIKE_TOGGLE_SUCCESS, result);
  }
}
