package com.jeju.nanaland.domain.nana.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.POST_LIKE_TOGGLE_SUCCESS;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetailDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
import com.jeju.nanaland.domain.nana.service.NanaService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
import com.jeju.nanaland.global.jwt.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nana")
@RequiredArgsConstructor
public class NanaController {

  private final NanaService nanaService;

  @Operation(
      summary = "메인 페이지 나나's pick 배너",
      description = "어플 메인페이지에 슬라이드되는 나나's pick thumbnail 4개 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping
  public BaseResponse<List<NanaResponse.NanaThumbnail>> nanaMainPage(@AuthMember Member member) {
    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SuccessCode.NANA_MAIN_SUCCESS,
        nanaService.getMainNanaThumbnails(locale));
  }

  @Operation(
      summary = "나나's pick 전체 조회",
      description = "나나's pick 페이지에서 전체 게시물 썸네일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<NanaThumbnailDto> nanaAll(@AuthMember Member member, int page,
      int size) {
    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SuccessCode.NANA_LIST_SUCCESS,
        nanaService.getNanaThumbnails(locale, page, size));
  }

  @Operation(
      summary = "나나's pick 게시물 상세조회",
      description = "나나's pick 개별 게시물 상세 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<NanaDetailDto> nanaDetail(@PathVariable(name = "id") Long id) {
    return BaseResponse.success(SuccessCode.NANA_DETAIL_SUCCESS,
        nanaService.getNanaDetail(id));
  }

  @Operation(summary = "좋아요 토글", description = "좋아요 토글 기능 (좋아요 상태 -> 좋아요 취소 상태, 좋아요 취소 상태 -> 좋아요 상태)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @PostMapping("/like/{id}")
  public BaseResponse<String> toggleLikeStatus(@AuthMember Member member, @PathVariable Long id) {
    String result = nanaService.toggleLikeStatus(member, id);
    return BaseResponse.success(POST_LIKE_TOGGLE_SUCCESS, result);
  }
}
