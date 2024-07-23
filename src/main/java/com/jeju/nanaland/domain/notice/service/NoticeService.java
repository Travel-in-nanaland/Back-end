package com.jeju.nanaland.domain.notice.service;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeListDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeTitleDto;
import com.jeju.nanaland.domain.notice.repository.NoticeRepository;
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
}
