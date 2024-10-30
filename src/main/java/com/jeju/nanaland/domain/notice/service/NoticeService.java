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

  /**
   * 공지사항 프리뷰 리스트 조회
   *
   * @param memberInfoDto 회원 정보
   * @param page          페이지 정보
   * @param size          페이지 크기 정보
   * @return 공지사항 프리뷰 리스트
   */
  public NoticeResponse.PreviewPageDto getNoticePreview(MemberInfoDto memberInfoDto, int page,
      int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NoticeResponse.PreviewDto> noticeList = noticeRepository.findAllNoticePreviewDtoOrderByCreatedAt(
        memberInfoDto.getLanguage(), pageable);

    return NoticeResponse.PreviewPageDto.builder()
        .totalElements(noticeList.getTotalElements())
        .data(noticeList.getContent())
        .build();
  }

  /**
   * 공지사항 상세 조회
   *
   * @param memberInfoDto 회원 정보
   * @param noticeId      공지사항 ID
   * @return 공지사항 상세 정보
   * @throws NotFoundException 존재하는 공지사항이 없는 경우
   */
  public NoticeResponse.DetailDto getNoticeDetail(MemberInfoDto memberInfoDto, Long noticeId) {
    // 공지사항이 존재하는지 확인
    noticeRepository.findById(noticeId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.NOTICE_NOT_FOUND.getMessage()));

    // 공지사항 상제 정보와 내용 조회
    NoticeResponse.DetailDto noticeDetailDto = noticeRepository.findNoticeDetailDto(
        memberInfoDto.getLanguage(), noticeId);

    if (noticeDetailDto == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
    }

    List<NoticeResponse.ContentDto> noticeContents = noticeRepository.findAllNoticeContentDto(
        memberInfoDto.getLanguage(), noticeId);

    noticeDetailDto.setNoticeContents(noticeContents);
    return noticeDetailDto;
  }
}
