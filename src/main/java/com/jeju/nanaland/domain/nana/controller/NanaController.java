package com.jeju.nanaland.domain.nana.controller;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.DetailPageDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.PreviewPageDto;
import com.jeju.nanaland.domain.nana.service.NanaService;
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

@Slf4j
@RestController
@RequestMapping("/nana")
@RequiredArgsConstructor
@Tag(name = "나나스픽(Nana)", description = "나나스픽(Nana) API입니다.")
public class NanaController {

  private final NanaService nanaService;

  @Operation(
      summary = "메인 페이지 나나's pick 배너",
      description = "어플 메인페이지에 슬라이드되는 나나's pick thumbnail 4개 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping
  public BaseResponse<List<PreviewDto>> getMainNanaThumbnails(
      @AuthMember MemberInfoDto memberInfoDto) {
    return BaseResponse.success(SuccessCode.NANA_MAIN_SUCCESS,
        nanaService.getMainPageNanaThumbnails(memberInfoDto.getLanguage()));
  }

  @Operation(
      summary = "금주 추천 나나's pick",
      description = "나나's pick 페이지 상단 금주 추천 게시물 4개 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/recommend")
  public BaseResponse<List<PreviewDto>> getRecommendNanaThumbnails(
      @AuthMember MemberInfoDto memberInfoDto) {
    return BaseResponse.success(SuccessCode.NANA_RECOMMEND_LIST_SUCCESS,
        nanaService.getRecommendNanaThumbnails(memberInfoDto.getLanguage()));
  }

  @Operation(
      summary = "나나's pick 리스트 조회",
      description = "나나's pick 페이지에서 4개 게시물 조회, 모두 보기 게시물 조회)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<PreviewPageDto> getNanaThumbnails(@AuthMember MemberInfoDto memberInfoDto,
      int page,
      int size) {
    return BaseResponse.success(SuccessCode.NANA_LIST_SUCCESS,
        nanaService.getNanaThumbnails(memberInfoDto.getLanguage(), page, size));
  }

  @Operation(
      summary = "나나's pick 게시물 상세조회",
      description = "나나's pick 개별 게시물 상세 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<DetailPageDto> getNanaDetail(@AuthMember MemberInfoDto memberInfoDto,
      @PathVariable(name = "id") Long id, @RequestParam(defaultValue = "false") boolean isSearch) {
    return BaseResponse.success(SuccessCode.NANA_DETAIL_SUCCESS,
        nanaService.getNanaDetail(memberInfoDto, id, isSearch));
  }

//  @GetMapping("/upload")
//  public ModelAndView getExistNanaListInfo() {
//    ModelAndView modelAndView = new ModelAndView("upload-nana.html");
//    modelAndView.addObject("nanaInfo", nanaService.getExistNanaListInfo());
//    return modelAndView;
//  }

//  @PostMapping("/upload")
//  public ModelAndView createNanaPick(@ModelAttribute NanaRequest.NanaUploadDto nanaUploadDto,
//      RedirectAttributes redirectAttributes) {
//    String result = nanaService.createNanaPick(nanaUploadDto);
//
//    redirectAttributes.addFlashAttribute("result", result);
//    return new ModelAndView("redirect:/nana/upload");
//  }
}
