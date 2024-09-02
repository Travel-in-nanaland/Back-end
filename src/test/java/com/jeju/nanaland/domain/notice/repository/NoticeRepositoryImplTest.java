package com.jeju.nanaland.domain.notice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeContentDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeDetailDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeTitleDto;
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
  @DisplayName("공지사항 리스트 조회")
  void findNoticeList() {
    // given
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Page<NoticeTitleDto> noticeList = noticeRepository.findNoticeList(
        memberInfoDto.getLanguage(), pageable);

    // then
    assertThat(noticeList).isNotNull();
    assertThat(noticeList.getTotalElements()).isEqualTo(3);
    assertThat(noticeList.getContent()).isNotEmpty();
  }

  @Test
  @DisplayName("공지사항 세부 정보 조회")
  void getNoticeDetail() {
    // given
    Notice notice = createNotice();
    NoticeTitle noticeTitle = createNoticeTitle(notice, Language.KOREAN);
    createNoticeContent(noticeTitle, null);
    createNoticeContent(noticeTitle, createImageFile());

    // when
    NoticeDetailDto noticeDetail = noticeRepository.getNoticeDetail(memberInfoDto.getLanguage(),
        notice.getId());

    // then
    assertThat(noticeDetail.getTitle()).isEqualTo(noticeTitle.getTitle());
    assertThat(noticeDetail.getCreatedAt()).isEqualTo(notice.getCreatedAt().toLocalDate());
  }

  @Test
  @DisplayName("공지사항 세부 내용 조회")
  void getNoticeContents() {
    // given
    Notice notice = createNotice();
    NoticeTitle noticeTitle = createNoticeTitle(notice, Language.KOREAN);
    NoticeContent noticeContent = createNoticeContent(noticeTitle, createImageFile());
    NoticeContent noticeContent2 = createNoticeContent(noticeTitle, null);

    // when
    List<NoticeContentDto> noticeContents = noticeRepository.getNoticeContents(
        memberInfoDto.getLanguage(), notice.getId());

    // then
    assertThat(noticeContents).hasSize(2);
    assertThat(noticeContents.get(0).getContent()).isEqualTo(noticeContent.getContent());
    assertThat(noticeContents.get(0).getImage().getThumbnailUrl()).isEqualTo(
        noticeContent.getImageFile().getThumbnailUrl());
    assertThat(noticeContents.get(1).getContent()).isEqualTo(noticeContent2.getContent());
    assertThat(noticeContents.get(1).getImage()).isNull();
  }
}