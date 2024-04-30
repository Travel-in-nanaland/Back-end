package com.jeju.nanaland.domain.report.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.POST_INFO_FIX_REPORT_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.report.dto.ReportRequest;
import com.jeju.nanaland.domain.report.service.ReportService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.jwt.AuthMember;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

  @PostMapping(value = "/info-fix",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse<String> requestPostInfoFix(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestPart("reqDto") @Valid ReportRequest.InfoFixDto reqDto,
      @Parameter(
          description = "정보 수정 요청 이미지파일",
          content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
      )
      @RequestPart(value = "multipartFile", required = false) MultipartFile multipartFile) {

    reportService.postInfoFixReport(memberInfoDto, reqDto, multipartFile);
    return BaseResponse.success(POST_INFO_FIX_REPORT_SUCCESS);
  }
}
