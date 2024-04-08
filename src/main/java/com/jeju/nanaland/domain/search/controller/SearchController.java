package com.jeju.nanaland.domain.search.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEARCH_SUCCESS;

import com.jeju.nanaland.domain.search.dto.SearchResponseDto;
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
  public ApiResponse<SearchResponseDto.Category> searchCategory(String title, Pageable pageable) {

    String locale = "KOREAN";
    return ApiResponse.success(SEARCH_SUCCESS,
        searchService.getCategorySearchResult(title, locale, pageable));
  }
}
