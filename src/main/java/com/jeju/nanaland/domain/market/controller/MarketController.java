package com.jeju.nanaland.domain.market.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.POST_LIKE_TOGGLE_SUCCESS;

import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.market.service.MarketService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/market")
@Slf4j
@Tag(name = "전통시장(Market)", description = "전통시장(Market) API입니다.")
public class MarketController {

  private final MarketService marketService;

  @Operation(summary = "좋아요 토글", description = "좋아요 토글 기능 (좋아요 상태 -> 좋아요 취소 상태, 좋아요 취소 상태 -> 좋아요 상태)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @PostMapping("/like/{id}")
  public BaseResponse<FavoriteResponse.StatusDto> toggleLikeStatus(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id) {

    return BaseResponse.success(
        POST_LIKE_TOGGLE_SUCCESS,
        marketService.toggleLikeStatus(memberInfoDto.getMember(), id));
  }
}
