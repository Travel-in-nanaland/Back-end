package com.jeju.nanaland.domain.festival.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.FESTIVAL_LIST_SUCCESS;

import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.service.FestivalService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/festival")
@Slf4j
@Tag(name = "축제(Festival)", description = "축제(Festival) API입니다.")
public class FestivalController {

  private final FestivalService festivalService;

//  @Operation(summary = "좋아요 토글", description = "좋아요 토글 기능 (좋아요 상태 -> 좋아요 취소 상태, 좋아요 취소 상태 -> 좋아요 상태)")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200", description = "성공"),
//      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
//      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
//  })
//  @GetMapping("/this-month")
//  public BaseResponse<String> getFestival(@AuthMember Member member,@RequestParam(name = "date")) {
//    Locale locale = member.getLanguage().getLocale();
//    festivalService
//    return BaseResponse.success(POST_LIKE_TOGGLE_SUCCESS, result);
//  }

  @Operation(summary = "종료된 축제 리스트 조회", description = "종료된 축제 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/past")
  public BaseResponse<FestivalThumbnailDto> getPastFestival(@AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    return BaseResponse.success(FESTIVAL_LIST_SUCCESS,
        festivalService.getPastFestivalList(memberInfoDto.getLanguage().getLocale(), page, size));
  }

  @Operation(summary = "계절별 축제 리스트 조회", description = "계절별 축제 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "계절 선택시 Query Parameter가 spring, summer, autumn, winter 가 아닌 경우", content = @Content),

  })
  @GetMapping("/season")
  public BaseResponse<FestivalThumbnailDto> getSeasonFestival(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "spring") String season) {

    return BaseResponse.success(FESTIVAL_LIST_SUCCESS,
        festivalService.getSeasonFestivalList(memberInfoDto.getLanguage().getLocale(), page, size,
            season));
  }
}
