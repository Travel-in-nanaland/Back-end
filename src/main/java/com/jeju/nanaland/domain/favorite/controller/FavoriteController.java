package com.jeju.nanaland.domain.favorite.controller;

import static com.jeju.nanaland.domain.common.data.CategoryContent.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.CategoryContent.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.CategoryContent.MARKET;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NANA;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NATURE;
import static com.jeju.nanaland.global.exception.SuccessCode.POST_LIKE_TOGGLE_SUCCESS;

import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest.LikeToggleDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.FavoriteThumbnailDto;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.StatusDto;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import com.jeju.nanaland.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public BaseResponse<FavoriteThumbnailDto> getAllFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteThumbnailDto resultDto = favoriteService.getAllFavoriteList(memberInfoDto, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "이색체험 찜리스트 조회", description = "이색체험 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/experience/list")
  public BaseResponse<FavoriteThumbnailDto> getExperienceFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteThumbnailDto resultDto =
        favoriteService.getCategoryFavoriteList(memberInfoDto, EXPERIENCE, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "7대자연 찜리스트 조회", description = "7대자연 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/nature/list")
  public BaseResponse<FavoriteThumbnailDto> getNatureFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteThumbnailDto resultDto =
        favoriteService.getCategoryFavoriteList(memberInfoDto, NATURE, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "축제 찜리스트 조회", description = "축제 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/festival/list")
  public BaseResponse<FavoriteThumbnailDto> getFestivalFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteThumbnailDto resultDto =
        favoriteService.getCategoryFavoriteList(memberInfoDto, FESTIVAL, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "전통시장 찜리스트 조회", description = "전통시장 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/market/list")
  public BaseResponse<FavoriteThumbnailDto> getMarketFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteThumbnailDto resultDto =
        favoriteService.getCategoryFavoriteList(memberInfoDto, MARKET, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "나나스픽 찜리스트 조회", description = "나나스픽 찜리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/nana/list")
  public BaseResponse<FavoriteThumbnailDto> getNanaFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteThumbnailDto resultDto =
        favoriteService.getCategoryFavoriteList(memberInfoDto, NANA, page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @Operation(summary = "좋아요 토글", description = "좋아요 토글 기능 (좋아요 상태 -> 좋아요 취소 상태, 좋아요 취소 상태 -> 좋아요 상태)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "요청이 잘못된 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "카테고리에 해당하는 게시물 id가 존재하지 않는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @PostMapping("/like")
  public BaseResponse<FavoriteResponse.StatusDto> toggleLikeStatus(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestBody @Valid LikeToggleDto likeToggleDto) {

    StatusDto statusDto = favoriteService.toggleLikeStatus(memberInfoDto, likeToggleDto);
    return BaseResponse.success(POST_LIKE_TOGGLE_SUCCESS, statusDto);
  }
}
