package com.jeju.nanaland.domain.festival.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.FESTIVAL_LIST_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.POST_LIKE_TOGGLE_SUCCESS;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.service.FestivalService;
import com.jeju.nanaland.domain.member.entity.Member;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @Operation(summary = "좋아요 토글", description = "좋아요 토글 기능 (좋아요 상태 -> 좋아요 취소 상태, 좋아요 취소 상태 -> 좋아요 상태)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @PostMapping("/like/{id}")
  public BaseResponse<String> toggleLikeStatus(@AuthMember Member member, @PathVariable Long id) {
    String result = festivalService.toggleLikeStatus(member, id);
    return BaseResponse.success(POST_LIKE_TOGGLE_SUCCESS, result);
  }

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
  public BaseResponse<FestivalThumbnailDto> getPastFestival(@AuthMember Member member,
      @RequestParam(defaultValue = "") String addressFilter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(FESTIVAL_LIST_SUCCESS,
        festivalService.getPastFestivalList(locale, page, size));
  }

  @Operation(summary = "계절별 축제 리스트 조회", description = "계절별 축제 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "계절 선택시 Query Parameter가 spring, summer, autumn, winter 가 아닌 경우", content = @Content),

  })
  @GetMapping("/past")
  public BaseResponse<FestivalThumbnailDto> getSeasonFestival(@AuthMember Member member,
      @RequestParam(defaultValue = "") String addressFilter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "spring") String season) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(FESTIVAL_LIST_SUCCESS,
        festivalService.getSeasonFestivalList(locale, page, size, season));
  }
}
