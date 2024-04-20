package com.jeju.nanaland.domain.search.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEARCH_SUCCESS;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.dto.SearchResponse.CategoryDto;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "검색(Search)", description = "검색(Search) API입니다.")
@Slf4j
public class SearchController {

  private final SearchService searchService;

  @Operation(
      summary = "카테고리 검색 (6대자연, 전통시장, 축제, 이색체험)",
      description = "각 카테고리별 title 파라미터가 포함된 제목의 게시물 검색, 각 카테고리 별로 총 개수와 썸네일 2개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/category")
  public BaseResponse<CategoryDto> searchCategory(
      @AuthMember Member member,
      @NotNull String keyword) {

    log.info("test");
    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SEARCH_SUCCESS,
        searchService.getCategorySearchResultDto(member, keyword, locale));
  }

  @Operation(
      summary = "자연 검색 결과",
      description = "자연 검색 결과 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/nature")
  public BaseResponse<SearchResponse.ResultDto> searchNature(
      @AuthMember Member member,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SEARCH_SUCCESS,
        searchService.getNatureSearchResultDto(member, keyword, locale, page, size));
  }

  @Operation(
      summary = "축제 검색 결과",
      description = "축제 검색 결과 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/festival")
  public BaseResponse<SearchResponse.ResultDto> searchFestival(
      @AuthMember Member member,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SEARCH_SUCCESS,
        searchService.getFestivalSearchResultDto(member, keyword, locale, page, size));
  }

  @Operation(
      summary = "이색체험 검색 결과",
      description = "이색체험 검색 결과 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/experience")
  public BaseResponse<SearchResponse.ResultDto> searchExperience(
      @AuthMember Member member,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SEARCH_SUCCESS,
        searchService.getExperienceSearchResultDto(member, keyword, locale, page, size));
  }

  @Operation(
      summary = "전통시장 검색 결과",
      description = "전통시장 검색 결과 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/market")
  public BaseResponse<SearchResponse.ResultDto> searchMarket(
      @AuthMember Member member,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SEARCH_SUCCESS,
        searchService.getMarketSearchResultDto(member, keyword, locale, page, size));
  }

  @Operation(
      summary = "인기 검색어 조회",
      description = "언어 별로 가장 검색이 많이 된 8개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/popular")
  public BaseResponse<List<String>> getPopularSearch(@AuthMember Member member) {

    Locale locale = member.getLanguage().getLocale();
    return BaseResponse.success(SEARCH_SUCCESS, searchService.getPopularSearch(locale));
  }
}
