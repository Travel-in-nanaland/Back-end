package com.jeju.nanaland.domain.favorite.controller;

import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
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
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "찜리스트(Favorite)", description = "찜리스트(Favorite) API입니다.")
public class FavoriteController {

  private final FavoriteService favoriteService;

  @Operation(summary = "전체 찜리스트 조회", description = "전체 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/all/list")
  public BaseResponse<FavoriteResponse.AllCategoryDto> getAllFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteResponse.AllCategoryDto resultDto =
        favoriteService.getAllFavoriteList(memberInfoDto, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "이색체험 찜리스트 조회", description = "이색체험 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/experience/list")
  public BaseResponse<FavoriteResponse.ExperienceDto> getExperienceFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteResponse.ExperienceDto resultDto =
        favoriteService.getExperienceFavoriteList(memberInfoDto, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "7대자연 찜리스트 조회", description = "7대자연 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/nature/list")
  public BaseResponse<FavoriteResponse.NatureDto> getNatureFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteResponse.NatureDto resultDto =
        favoriteService.getNatureFavoriteList(memberInfoDto, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "축제 찜리스트 조회", description = "축제 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/festival/list")
  public BaseResponse<FavoriteResponse.FestivalDto> getFestivalFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteResponse.FestivalDto resultDto =
        favoriteService.getFestivalFavoriteList(memberInfoDto, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "전통시장 찜리스트 조회", description = "전통시장 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/market/list")
  public BaseResponse<FavoriteResponse.MarketDto> getMarketFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteResponse.MarketDto resultDto =
        favoriteService.getMarketFavoriteList(memberInfoDto, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  // TODO: NANA 찜리스트
//  @GetMapping("/nana/list")
//  public BaseResponse<FavoriteResponse.AllCategoryDto> getAllFavoriteList() {
//    return null;
//  }
}