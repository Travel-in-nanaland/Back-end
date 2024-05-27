package com.jeju.nanaland.domain.nature.controller;


import static com.jeju.nanaland.global.exception.SuccessCode.NATURE_LIST_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureDetailDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.NatureThumbnailDto;
import com.jeju.nanaland.domain.nature.service.NatureService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import com.jeju.nanaland.global.exception.SuccessCode;
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
@RequestMapping("/nature")
@Slf4j
@Tag(name = "7대자연(Nature)", description = "7대자연(Nature) API입니다.")
public class NatureController {

  private final NatureService natureService;

  @Operation(summary = "7대 자연 리스트 조회", description = "7대 자연 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<NatureThumbnailDto> getNatureList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "") List<String> addressFilterList,
      String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {
    NatureThumbnailDto data = natureService.getNatureList(memberInfoDto, addressFilterList, keyword,
        page, size);
    return BaseResponse.success(NATURE_LIST_SUCCESS, data);
  }

  @Operation(summary = "7대 자연 상세 정보 조회", description = "7대 자연 상세 정보 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "해당 id의 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버측 에러", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<NatureDetailDto> getNatureDetail(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean isSearch) {
    NatureDetailDto natureDetail = natureService.getNatureDetail(memberInfoDto, id, isSearch);
    return BaseResponse.success(SuccessCode.NATURE_DETAIL_SUCCESS, natureDetail);
  }
}
