package com.jeju.nanaland.domain.market.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.MARKET_DETAIL_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.MARKET_LIST_SUCCESS;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketDetailDto;
import com.jeju.nanaland.domain.market.service.MarketService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/market")
@Slf4j
@Tag(name = "전통시장(Market)", description = "전통시장(Market) API입니다.")
public class MarketController {

  private final MarketService marketService;

  @Operation(summary = "전통시장 리스트 조회", description = "전통시장 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<MarketResponse.MarketThumbnailDto> getMarketList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    MarketResponse.MarketThumbnailDto thumbnailDto = marketService.getMarketList(memberInfoDto,
        addressFilterList, page, size);

    return BaseResponse.success(MARKET_LIST_SUCCESS, thumbnailDto);
  }

  @Operation(summary = "전통시장 상세 정보 조회", description = "전통시장 상세 정보 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<MarketResponse.MarketDetailDto> getMarketDetail(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean isSearch) {

    MarketDetailDto marketDetail = marketService.getMarketDetail(memberInfoDto, id, isSearch);
    return BaseResponse.success(MARKET_DETAIL_SUCCESS, marketDetail);
  }
}
