package com.jeju.nanaland.domain.notice.service;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse;
import com.jeju.nanaland.domain.notice.repository.NoticeRepository;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {

  private final NoticeRepository noticeRepository;

  // 공지사항 리스트 조회
  public NoticeResponse.CardDto getNoticeCard(MemberInfoDto memberInfoDto, int page,
      int size) {

    Pageable pageable = PageRequest.of(page, size);

    Page<NoticeResponse.TitleDto> noticeList = noticeRepository.findNoticeList(
        memberInfoDto.getLanguage(), pageable);

    return NoticeResponse.CardDto.builder()
        .totalElements(noticeList.getTotalElements())
        .data(noticeList.getContent())
        .build();
  }

  // 공지사항 상세 조회
  public NoticeResponse.DetailDto getNoticeDetail(MemberInfoDto memberInfoDto, Long id) {
    // 공지사항이 존재하는지 확인
    noticeRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NOTICE_NOT_FOUND.getMessage()));

    // 공지사항 세부정보와 내용 조회
    NoticeResponse.DetailDto noticeDetailDto = noticeRepository.getNoticeDetail(
        memberInfoDto.getLanguage(), id);

    if (noticeDetailDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    List<NoticeResponse.ContentDto> noticeContents = noticeRepository.getNoticeContents(
        memberInfoDto.getLanguage(), id);

    noticeDetailDto.setNoticeContents(noticeContents);
    return noticeDetailDto;
  }
}
