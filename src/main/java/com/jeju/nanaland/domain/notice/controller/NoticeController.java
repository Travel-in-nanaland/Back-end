package com.jeju.nanaland.domain.notice.controller;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeListDto;
import com.jeju.nanaland.domain.notice.service.NoticeService;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
@Tag(name = "공지사항(Notice)", description = "공지사항 API입니다.")
public class NoticeController {

  private final NoticeService noticeService;

  @GetMapping("/list")
  public ResponseEntity getNoticeList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size
  ) {
    NoticeListDto noticeList = noticeService.getNoticeList(memberInfoDto, page, size);
    return ResponseEntity.ok(noticeList);
  }
}
