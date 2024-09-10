package com.jeju.nanaland.domain.notice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse;
import com.jeju.nanaland.domain.notice.entity.Notice;
import com.jeju.nanaland.domain.notice.entity.NoticeCategory;
import com.jeju.nanaland.domain.notice.repository.NoticeRepository;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class NoticeServiceTest {

  ImageFile imageFile;
  Member member;
  MemberInfoDto memberInfoDto;

  @InjectMocks
  NoticeService noticeService;

  @Mock
  NoticeRepository noticeRepository;

  @BeforeEach
  void setUp() {
    imageFile = createImageFile();
    Language language = Language.KOREAN;
    member = createMember(language);
    memberInfoDto = createMemberInfoDto(language, member);
  }

  private ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
  }

  private Member createMember(Language language) {
    return spy(Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .travelType(TravelType.GAMGYUL)
        .build());
  }

  private MemberInfoDto createMemberInfoDto(Language language, Member member) {
    return MemberInfoDto.builder()
        .language(language)
        .member(member)
        .build();
  }

  private Page<NoticeResponse.PreviewDto> createNoticeTitles() {
    List<NoticeResponse.PreviewDto> noticeTitleDtos = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      noticeTitleDtos.add(
          NoticeResponse.PreviewDto.builder()
              .noticeCategory(NoticeCategory.NOTICE.name())
              .title("title")
              .build());
    }

    return new PageImpl<>(noticeTitleDtos, PageRequest.of(0, 2), 10);
  }

  private Notice createNotice() {
    return Notice.builder()
        .noticeCategory(NoticeCategory.NOTICE)
        .build();
  }

  private NoticeResponse.DetailDto createNoticeDetail() {
    return NoticeResponse.DetailDto.builder()
        .title("title")
        .build();
  }

  private List<NoticeResponse.ContentDto> createNoticeContents() {
    List<NoticeResponse.ContentDto> noticeContents = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      noticeContents.add(
          NoticeResponse.ContentDto.builder()
              .image(new ImageFileDto("origin", "thumbnail"))
              .content("content")
              .build());
    }

    return noticeContents;
  }

  @Test
  @DisplayName("공지사항 리스트 조회 성공")
  void getNoticeList() {
    // given
    Page<NoticeResponse.PreviewDto> noticeTitleDtos = createNoticeTitles();
    doReturn(noticeTitleDtos).when(noticeRepository).findAllNoticePreviewDtoOrderByCreatedAt(any(Language.class), any());

    // when
    NoticeResponse.PreviewPageDto noticeCardDto = noticeService.getNoticeCard(memberInfoDto, 0, 12);

    // then
    assertThat(noticeCardDto).isNotNull();
    assertThat(noticeCardDto.getTotalElements()).isEqualTo(
        noticeTitleDtos.getTotalElements());
    assertThat(noticeCardDto.getData()).hasSameSizeAs(noticeTitleDtos.getContent());
  }

  @Test
  @DisplayName("공지사항 상세 조회 실패 - 데이터가 없는 경우")
  void getNoticeDetailFail() {
    // given
    doReturn(Optional.empty()).when(noticeRepository).findById(any());

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> noticeService.getNoticeDetail(memberInfoDto, 1L));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(
        ErrorCode.NOTICE_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("공지사항 상세 조회 실패 - 데이터가 없는 경우")
  void getNoticeDetailFail2() {
    // given
    Notice notice = createNotice();
    doReturn(Optional.of(notice)).when(noticeRepository).findById(any());
    doReturn(null).when(noticeRepository).findNoticeDetailDto(any(Language.class), any());

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> noticeService.getNoticeDetail(memberInfoDto, 1L));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(
        ErrorCode.NOT_FOUND_EXCEPTION.getMessage());
  }

  @Test
  @DisplayName("공지사항 상세 조회 성공")
  void getNoticeDetailSuccess() {
    // given
    Notice notice = createNotice();
    NoticeResponse.DetailDto noticeDetail = createNoticeDetail();
    List<NoticeResponse.ContentDto> noticeContents = createNoticeContents();

    doReturn(Optional.of(notice)).when(noticeRepository).findById(any());
    doReturn(noticeDetail).when(noticeRepository).findNoticeDetailDto(any(Language.class), any());
    doReturn(noticeContents).when(noticeRepository).findAllNoticeContentDto(any(Language.class), any());

    // when
    NoticeResponse.DetailDto result = noticeService.getNoticeDetail(memberInfoDto, 1L);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(noticeDetail.getTitle());
    assertThat(result.getNoticeContents()).hasSameSizeAs(noticeContents);
    assertThat(result.getNoticeContents().get(0).getContent()).isEqualTo(
        noticeContents.get(0).getContent());
  }
}