package com.jeju.nanaland.domain.nana.controller;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nana.dto.NanaRequest;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaDetailDto;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailDto;
import com.jeju.nanaland.domain.nana.service.NanaService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import com.jeju.nanaland.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RestController
@RequestMapping("/nana")
@RequiredArgsConstructor
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
  public BaseResponse<List<NanaResponse.NanaThumbnail>> nanaMainPage(
      @AuthMember MemberInfoDto memberInfoDto) {
    return BaseResponse.success(SuccessCode.NANA_MAIN_SUCCESS,
        nanaService.getMainNanaThumbnails(memberInfoDto.getLanguage().getLocale()));
  }

  @Operation(
      summary = "나나's pick 전체 조회",
      description = "나나's pick 페이지에서 전체 게시물 썸네일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<NanaThumbnailDto> nanaAll(@AuthMember MemberInfoDto memberInfoDto, int page,
      int size) {
    return BaseResponse.success(SuccessCode.NANA_LIST_SUCCESS,
        nanaService.getNanaThumbnails(memberInfoDto.getLanguage().getLocale(), page, size));
  }

  @Operation(
      summary = "나나's pick 게시물 상세조회",
      description = "나나's pick 개별 게시물 상세 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<NanaDetailDto> nanaDetail(@AuthMember MemberInfoDto memberInfoDto,
      @PathVariable(name = "id") Long id, @RequestParam(defaultValue = "false") boolean isSearch) {
    return BaseResponse.success(SuccessCode.NANA_DETAIL_SUCCESS,
        nanaService.getNanaDetail(memberInfoDto, id, isSearch));
  }

  @GetMapping("/upload")
  public ModelAndView getUploadNana() {
    ModelAndView modelAndView = new ModelAndView("upload-nana.html");
    modelAndView.addObject("nanaInfo", nanaService.getExistNanaListInfo());
    return modelAndView;
  }

  @PostMapping("/upload")
  public ModelAndView uploadNana(@ModelAttribute NanaRequest.NanaUploadDto nanaUploadDto,
      RedirectAttributes redirectAttributes) {
    String result = nanaService.createNanaPick(nanaUploadDto);

    redirectAttributes.addFlashAttribute("result", result);
    return new ModelAndView("redirect:/nana/upload");
  }
}
