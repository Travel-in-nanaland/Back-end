package com.jeju.nanaland.domain.search.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEARCH_SUCCESS;

import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
@RequiredArgsConstructor
public class SearchController {

  private final SearchService searchService;

  @GetMapping("/category")
  public ApiResponse<SearchResponse.CategoryDto> searchCategory(String title) {
    /**
     * TODO: JWT로부터 locale 추출
     */
    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getCategorySearchResultDto(title, locale));
  }

  @GetMapping("/stay")
  public ApiResponse<SearchResponse.ResultDto> searchStay(String title, Pageable pageable) {
    /**
     * TODO: JWT로부터 locale 추출
     */
    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getStaySearchResultDto(title, locale, pageable));
  }

  @GetMapping("/nature")
  public ApiResponse<SearchResponse.ResultDto> searchNature(String title, Pageable pageable) {
    /**
     * TODO: JWT로부터 locale 추출
     */
    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getNatureSearchResultDto(title, locale, pageable));
  }

  @GetMapping("/festival")
  public ApiResponse<SearchResponse.ResultDto> searchFestival(String title, Pageable pageable) {
    /**
     * TODO: JWT로부터 locale 추출
     */
    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getFestivalSearchResultDto(title, locale, pageable));
  }

  @GetMapping("/experience")
  public ApiResponse<SearchResponse.ResultDto> searchExperience(String title, Pageable pageable) {
    /**
     * TODO: JWT로부터 locale 추출
     */
    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getExperienceSearchResultDto(title, locale, pageable));
  }

  @GetMapping("/market")
  public ApiResponse<SearchResponse.ResultDto> searchMarket(String title, Pageable pageable) {
    /**
     * TODO: JWT로부터 locale 추출
     */
    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getMarketSearchResultDto(title, locale, pageable));
  }
}
