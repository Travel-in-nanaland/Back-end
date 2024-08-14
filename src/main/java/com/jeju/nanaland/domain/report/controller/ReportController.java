package com.jeju.nanaland.domain.report.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.POST_INFO_FIX_REPORT_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.POST_REVIEW_REPORT_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.report.dto.ReportRequest.ClaimReportDto;
import com.jeju.nanaland.domain.report.dto.ReportRequest.InfoFixDto;
import com.jeju.nanaland.domain.report.service.ReportService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
@Slf4j
@Tag(name = "건의 사항(Report)", description = "건의 사항(Report) API입니다.")
public class ReportController {

  private final ReportService reportService;

  @Operation(summary = "정보 수정 제안", description = "게시물 id와 카테고리를 통해 게시물 정보 수정 제안 요청")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 형식 오류, category로 NANA 요청)", content = @Content),
      @ApiResponse(responseCode = "404", description = "해당 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "사진파일 업로드 실패 또는 관리자에게로 메일 전송 실패", content = @Content)
  })
  @PostMapping(value = "/info-fix",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse<String> requestPostInfoFix(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestPart("reqDto") @Valid InfoFixDto reqDto,
      @Parameter(
          description = "정보 수정 요청 이미지파일 리스트",
          content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
      )
      @RequestPart(value = "multipartFileList", required = false) List<MultipartFile> imageList) {

    reportService.requestPostInfoFix(memberInfoDto, reqDto, imageList);
    return BaseResponse.success(POST_INFO_FIX_REPORT_SUCCESS);
  }

  @Operation(summary = "신고 기능")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청인 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "해당 게시물이 없는 경우", content = @Content),
      @ApiResponse(responseCode = "500", description = "사진파일 업로드 실패 또는 관리자에게로 메일 전송 실패", content = @Content)
  })
  @PostMapping(value = "/claim",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse<String> requestClaimReport(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestPart("reqDto") @Valid ClaimReportDto reqDto,
      @Parameter(
          description = "신고 요청 파일 리스트",
          content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
      )
      @RequestPart(value = "multipartFileList", required = false) List<MultipartFile> fileList) {
    reportService.requestClaimReport(memberInfoDto, reqDto, fileList);
    return BaseResponse.success(POST_REVIEW_REPORT_SUCCESS);
  }
}
