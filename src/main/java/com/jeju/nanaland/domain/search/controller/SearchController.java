package com.jeju.nanaland.domain.search.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.SEARCH_SUCCESS;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.search.dto.SearchResponse;
import com.jeju.nanaland.domain.search.dto.SearchResponse.AllCategoryDto;
import com.jeju.nanaland.domain.search.dto.SearchResponse.ResultDto;
import com.jeju.nanaland.domain.search.dto.SearchResponse.SearchVolumeDto;
import com.jeju.nanaland.domain.search.service.SearchService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import com.jeju.nanaland.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
      summary = "카테고리 검색 (자연, 축제, 액티비티, 문화예술, 전통시장, 맛집, 나나스픽)",
      description = "각 카테고리별 title 파라미터가 포함된 제목의 게시물 검색, 각 카테고리 별로 총 개수와 썸네일 2개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/all")
  public BaseResponse<AllCategoryDto> searchAll(
      @AuthMember MemberInfoDto memberInfoDto,
      @NotNull String keyword) {

    SearchResponse.AllCategoryDto result = searchService.searchAll(memberInfoDto, keyword);
    return BaseResponse.success(SEARCH_SUCCESS, result);
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
      @AuthMember MemberInfoDto memberInfoDto,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList) {

    ResultDto result = searchService.searchNature(memberInfoDto, keyword, page, size,
        addressFilterList);
    return BaseResponse.success(SEARCH_SUCCESS, result);
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
      @AuthMember MemberInfoDto memberInfoDto,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList,
      @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyyMMdd")
      LocalDate startDate,
      @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyyMMdd")
      LocalDate endDate) {

    ResultDto result = searchService.searchFestival(memberInfoDto, keyword, page, size,
        addressFilterList, startDate, endDate);
    return BaseResponse.success(SEARCH_SUCCESS, result);
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
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam ExperienceType experienceType,
      @NotNull String keyword,
      @RequestParam(defaultValue = "") List<ExperienceTypeKeyword> keywordFilterList,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    ResultDto result = searchService.searchExperience(memberInfoDto, experienceType, keyword,
        keywordFilterList, addressFilterList, page, size);
    return BaseResponse.success(SEARCH_SUCCESS, result);
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
      @AuthMember MemberInfoDto memberInfoDto,
      @NotNull String keyword,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    ResultDto result = searchService.searchMarket(memberInfoDto, keyword, addressFilterList, page,
        size);
    return BaseResponse.success(SEARCH_SUCCESS, result);
  }

  @Operation(
      summary = "나나스픽 검색 결과",
      description = "나나스픽 검색 결과 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/nana")
  public BaseResponse<SearchResponse.ResultDto> searchNana(
      @AuthMember MemberInfoDto memberInfoDto,
      @NotNull String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    ResultDto result = searchService.searchNana(memberInfoDto, keyword, page, size);
    return BaseResponse.success(SEARCH_SUCCESS, result);
  }

  @Operation(
      summary = "제주 맛집 검색 결과",
      description = "제주 맛집 검색 결과 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/restaurant")
  public BaseResponse<SearchResponse.ResultDto> searchRestaurant(
      @AuthMember MemberInfoDto memberInfoDto,
      @NotNull String keyword,
      @RequestParam(defaultValue = "") List<RestaurantTypeKeyword> keywordFilter,
      @RequestParam(defaultValue = "") List<AddressTag> addressFilterList,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    ResultDto result = searchService.searchRestaurant(memberInfoDto, keyword, keywordFilter,
        addressFilterList, page, size);
    return BaseResponse.success(SEARCH_SUCCESS, result);
  }

  @Operation(
      summary = "인기 검색어 조회",
      description = "언어 별로 가장 검색이 많이 된 8개 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/popular")
  public BaseResponse<List<String>> getPopularSearch(@AuthMember MemberInfoDto memberInfoDto) {

    List<String> result = searchService.getPopularSearch(memberInfoDto.getLanguage());
    return BaseResponse.success(SEARCH_SUCCESS, result);
  }

  @Operation(
      summary = "검색량 UP 게시물 조회",
      description = "검색량이 높은 4개 게시물 반환")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "해당 id의 게시물이 없는 경우", content = @Content)
  })
  @GetMapping("/volume")
  public BaseResponse<List<SearchVolumeDto>> getTopSearchVolumePosts(
      @AuthMember MemberInfoDto memberInfoDto) {
    List<SearchVolumeDto> topSearchVolumePosts = searchService.getTopSearchVolumePosts(
        memberInfoDto);
    return BaseResponse.success(SuccessCode.SEARCH_VOLUME_SUCCESS, topSearchVolumePosts);
  }
}
