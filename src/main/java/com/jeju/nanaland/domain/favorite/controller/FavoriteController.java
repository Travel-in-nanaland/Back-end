package com.jeju.nanaland.domain.favorite.controller;

import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.exception.SuccessCode;
import com.jeju.nanaland.global.jwt.AuthMember;
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
public class FavoriteController {

  private final FavoriteService favoriteService;

  @GetMapping("/all/list")
  public BaseResponse<FavoriteResponse.AllCategoryDto> getAllFavoriteList() {
    return null;
  }

  @GetMapping("/experience/list")
  public BaseResponse<FavoriteResponse.ExperienceDto> getExperienceFavoriteList() {
    return null;
  }

  @GetMapping("/nature/list")
  public BaseResponse<FavoriteResponse.NatureDto> getNatureFavoriteList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    FavoriteResponse.NatureDto resultDto = favoriteService.getNatureFavoriteList(memberInfoDto,
        page, size);
    return BaseResponse.success(SuccessCode.GET_FAVORITE_LIST_SUCCESS, resultDto);
  }

  @GetMapping("/festival/list")
  public BaseResponse<FavoriteResponse.FestivalDto> getFestivalFavoriteList() {
    return null;
  }

  @GetMapping("/market/list")
  public BaseResponse<FavoriteResponse.MarketDto> getMarketFavoriteList() {
    return null;
  }

//  @GetMapping("/nana/list")
//  public BaseResponse<FavoriteResponse.AllCategoryDto> getAllFavoriteList() {
//    return null;
//  }
}
