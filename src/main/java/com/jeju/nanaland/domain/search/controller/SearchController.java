package com.jeju.nanaland.domain.search.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEARCH_SUCCESS;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.ApiResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

  private final SearchService searchService;

  @GetMapping("/category")
  public ApiResponse<SearchResponse.CategoryDto> searchCategory(
      @AuthMember Member member, String title) {

    Locale locale = member.getLanguage().getLocale();
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getCategorySearchResultDto(title, locale));
  }

  @GetMapping("/nature")
  public ApiResponse<SearchResponse.ResultDto> searchNature(
      @AuthMember Member member, String title, Pageable pageable) {

    Locale locale = member.getLanguage().getLocale();
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getNatureSearchResultDto(title, locale, pageable));
  }

  @GetMapping("/festival")
  public ApiResponse<SearchResponse.ResultDto> searchFestival(
      @AuthMember Member member, String title, Pageable pageable) {

    Locale locale = member.getLanguage().getLocale();
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getFestivalSearchResultDto(title, locale, pageable));
  }

  @GetMapping("/experience")
  public ApiResponse<SearchResponse.ResultDto> searchExperience(
      @AuthMember Member member, String title, Pageable pageable) {

    Locale locale = member.getLanguage().getLocale();
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getExperienceSearchResultDto(title, locale, pageable));
  }

  @GetMapping("/market")
  public ApiResponse<SearchResponse.ResultDto> searchMarket(
      @AuthMember Member member, String title, Pageable pageable) {

    Locale locale = member.getLanguage().getLocale();
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getMarketSearchResultDto(title, locale, pageable));
  }
}
