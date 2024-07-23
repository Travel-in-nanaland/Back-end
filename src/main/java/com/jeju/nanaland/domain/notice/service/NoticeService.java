package com.jeju.nanaland.domain.notice.service;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeContentDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeDetailDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeListDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeTitleDto;
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

  public NoticeListDto getNoticeList(MemberInfoDto memberInfoDto, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    Page<NoticeTitleDto> noticeList = noticeRepository.findNoticeList(memberInfoDto.getLanguage(),
        pageable);

    return NoticeListDto.builder()
        .totalElements(noticeList.getTotalElements())
        .data(noticeList.getContent())
        .build();
  }

  public NoticeDetailDto getNoticeDetail(MemberInfoDto memberInfoDto, Long id) {
    // 공지사항이 존재하는지 확인
    noticeRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NOTICE_NOT_FOUND.getMessage()));

    // 공지사항 세부정보와 내용 조회
    NoticeDetailDto noticeDetailDto = noticeRepository.getNoticeDetail(memberInfoDto.getLanguage(),
        id);

    if (noticeDetailDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    List<NoticeContentDto> noticeContents = noticeRepository.getNoticeContents(
        memberInfoDto.getLanguage(), id);

    noticeDetailDto.setNoticeContents(noticeContents);
    return noticeDetailDto;
  }
}
