package com.jeju.nanaland.domain.notice.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.NOTICE_DETAIL_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.NOTICE_LIST_SUCCESS;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeDetailDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeListDto;
import com.jeju.nanaland.domain.notice.service.NoticeService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
@Tag(name = "공지사항(Notice)", description = "공지사항 API입니다.")
public class NoticeController {

  private final NoticeService noticeService;

  @Operation(summary = "공지사항 리스트 조회", description = "공지사항 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
  })
  @GetMapping("/list")
  public BaseResponse<NoticeListDto> getNoticeList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size
  ) {
    NoticeListDto noticeList = noticeService.getNoticeList(memberInfoDto, page, size);
    return BaseResponse.success(NOTICE_LIST_SUCCESS, noticeList);
  }

  @Operation(summary = "공지사항 상세 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @GetMapping("/{id}")
  public BaseResponse<NoticeDetailDto> getNoticeDetail(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id) {
    NoticeDetailDto noticeDetailDto = noticeService.getNoticeDetail(memberInfoDto, id);
    return BaseResponse.success(NOTICE_DETAIL_SUCCESS, noticeDetailDto);
  }
}
