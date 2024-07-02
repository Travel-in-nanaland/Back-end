package com.jeju.nanaland.domain.experience.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.EXPERIENCE_LIST_SUCCESS;

import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnailDto;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.service.ExperienceService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/experience")
@Slf4j
@Tag(name = "이색체험(experience)", description = "이색체험(experience) API입니다.")
public class ExperienceController {

  private final ExperienceService experienceService;

  @Operation(summary = "액티비티 리스트 조회", description = "액티비티 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/activity/list")
  public BaseResponse<ExperienceThumbnailDto> getActivityList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "") List<String> keywordFilterList,
      @RequestParam(defaultValue = "") List<String> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    ExperienceThumbnailDto thumbnailDto = experienceService.getExperienceList(memberInfoDto,
        ExperienceType.ACTIVITY, keywordFilterList, addressFilterList, page, size);

    return BaseResponse.success(EXPERIENCE_LIST_SUCCESS, thumbnailDto);
  }

  @Operation(summary = "문화예술 리스트 조회", description = "문화예술 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/culture-and-arts/list")
  public BaseResponse<ExperienceThumbnailDto> getCultureAndArtsList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "") List<String> keywordFilterList,
      @RequestParam(defaultValue = "") List<String> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    ExperienceThumbnailDto thumbnailDto = experienceService.getExperienceList(memberInfoDto,
        ExperienceType.CULTURE_AND_ARTS, keywordFilterList, addressFilterList, page, size);

    return BaseResponse.success(EXPERIENCE_LIST_SUCCESS, thumbnailDto);
  }
}
