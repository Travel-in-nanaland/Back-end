package com.jeju.nanaland.domain.restaurant.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.RESTAURANT_DETAIL_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.RESTAURANT_LIST_SUCCESS;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantDetailDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnailDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.restaurant.service.RestaurantService;
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
@RequestMapping("/restaurant")
@Slf4j
@Tag(name = "제주 맛집(restaurant)", description = "제주 맛집(restaurant) API입니다.")
public class RestaurantController {

  private final RestaurantService restaurantService;

  @Operation(summary = "제주 맛집 리스트 조회", description = "제주 맛집 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<RestaurantThumbnailDto> getRestaurantList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "") List<RestaurantTypeKeyword> keywordFilter,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    RestaurantThumbnailDto thumbnailDto = restaurantService.getRestaurantList(memberInfoDto,
        keywordFilter, addressFilterList, page, size);

    return BaseResponse.success(RESTAURANT_LIST_SUCCESS, thumbnailDto);
  }

  @Operation(summary = "제주맛집 상세 정보 조회", description = "제주맛집 상세 정보 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "필요한 입력이 없는 경우 또는 해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<RestaurantDetailDto> getRestaurantDetail(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean isSearch) {

    RestaurantDetailDto restaurantDetail = restaurantService.getRestaurantDetail(memberInfoDto,
        id, isSearch);
    return BaseResponse.success(RESTAURANT_DETAIL_SUCCESS, restaurantDetail);
  }
}
