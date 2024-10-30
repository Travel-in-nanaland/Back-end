package com.jeju.nanaland.domain.notice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse;
import com.jeju.nanaland.domain.notice.entity.Notice;
import com.jeju.nanaland.domain.notice.entity.NoticeCategory;
import com.jeju.nanaland.domain.notice.entity.NoticeContent;
import com.jeju.nanaland.domain.notice.entity.NoticeTitle;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoticeRepositoryImplTest {

  ImageFile imageFile;
  Member member;
  MemberInfoDto memberInfoDto;

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  private NoticeRepository noticeRepository;

  private ImageFile createImageFile() {
    imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    entityManager.persist(imageFile);
    return imageFile;
  }

  private MemberInfoDto createMemberInfoDto() {
    Language language = Language.KOREAN;
    member = Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .travelType(TravelType.NONE)
        .build();
    entityManager.persist(member);

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  private Notice createNotice() {
    Notice notice = Notice.builder()
        .noticeCategory(NoticeCategory.NOTICE)
        .build();
    entityManager.persist(notice);
    return notice;
  }

  private NoticeTitle createNoticeTitle(Notice notice, Language language) {
    NoticeTitle noticeTitle = NoticeTitle.builder()
        .notice(notice)
        .language(language)
        .title("title")
        .build();
    entityManager.persist(noticeTitle);
    return noticeTitle;
  }

  private NoticeContent createNoticeContent(NoticeTitle noticeTitle, ImageFile imageFile) {
    NoticeContent noticeContent = NoticeContent.builder()
        .noticeTitle(noticeTitle)
        .content("content")
        .imageFile(imageFile)
        .build();
    entityManager.persist(noticeContent);
    return noticeContent;
  }

  private void createNoticeList() {
    for (int i = 0; i < 3; i++) {
      Notice notice = createNotice();
      NoticeTitle noticeTitle1 = createNoticeTitle(notice, Language.KOREAN);
      NoticeTitle noticeTitle2 = createNoticeTitle(notice, Language.ENGLISH);
      createNoticeContent(noticeTitle1, createImageFile());
      createNoticeContent(noticeTitle1, null);
      createNoticeContent(noticeTitle2, createImageFile());
      createNoticeContent(noticeTitle2, null);
    }
  }

  @BeforeEach
  void setUp() {
    imageFile = createImageFile();
    memberInfoDto = createMemberInfoDto();
    createNoticeList();
  }

  @Test
  @DisplayName("공지사항 프리뷰 페이징 조회 TEST")
  void findAllNoticePreviewDtoOrderByCreatedAt() {
    // given: 페이징 정보 설정
    int page = 0;
    int size = 2;
    Pageable pageable = PageRequest.of(page, size);

    // when: 공지사항 프리뷰 페이징 조회
    Page<NoticeResponse.PreviewDto> result = noticeRepository.findAllNoticePreviewDtoOrderByCreatedAt(
        memberInfoDto.getLanguage(), pageable);

    // then: 조회된 공지사항 프리뷰 페이징 검증
    assertThat(result).isNotNull();
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getNumber()).isEqualTo(page);
    assertThat(result.getSize()).isEqualTo(size);
  }

  @Test
  @DisplayName("공지사항 상세 정보 조회 TEST")
  void findNoticeDetailDto() {
    // given: 공지사항 설정
    Notice notice = createNotice();
    NoticeTitle noticeTitle = createNoticeTitle(notice, Language.KOREAN);
    createNoticeContent(noticeTitle, null);
    createNoticeContent(noticeTitle, createImageFile());

    // when: 공지사항 상세 정보 조회
    NoticeResponse.DetailDto result = noticeRepository.findNoticeDetailDto(memberInfoDto.getLanguage(),
        notice.getId());

    // then: 조회된 공지사항 상세 정보 검증
    assertThat(result.getTitle()).isEqualTo(noticeTitle.getTitle());
    assertThat(result.getCreatedAt()).isEqualTo(notice.getCreatedAt().toLocalDate());
  }

  @Test
  @DisplayName("공지사항 내용 조회 TEST")
  void findAllNoticeContentDto() {
    // given: 공지사항 설정
    Notice notice = createNotice();
    NoticeTitle noticeTitle = createNoticeTitle(notice, Language.KOREAN);
    NoticeContent noticeContent = createNoticeContent(noticeTitle, createImageFile());
    NoticeContent noticeContent2 = createNoticeContent(noticeTitle, null);

    // when: 공지사항 내용 조회
    List<NoticeResponse.ContentDto> result = noticeRepository.findAllNoticeContentDto(
        memberInfoDto.getLanguage(), notice.getId());

    // then: 공지사항 내용 검증
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo(noticeContent.getContent());
    assertThat(result.get(0).getImage().getThumbnailUrl()).isEqualTo(
        noticeContent.getImageFile().getThumbnailUrl());
    assertThat(result.get(1).getContent()).isEqualTo(noticeContent2.getContent());
    assertThat(result.get(1).getImage()).isNull();
  }
}