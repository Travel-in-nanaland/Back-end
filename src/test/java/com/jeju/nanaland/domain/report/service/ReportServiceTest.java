package com.jeju.nanaland.domain.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.report.dto.ReportRequest.InfoFixDto;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReportServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  ReportService reportService;

  ImageFile imageFile1;
  Language language;
  Member member;
  MemberInfoDto memberInfoDto;

  @BeforeEach
  void init() {
    // imageFile
    imageFile1 = ImageFile.builder()
        .originUrl("origin1")
        .thumbnailUrl("thumbnail1")
        .build();
    em.persist(imageFile1);

    // member
    member = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId("123456789")
        .nickname("nickname1")
        .language(Language.KOREAN)
        .profileImageFile(imageFile1)
        .travelType(TravelType.NONE)
        .build();
    em.persist(member);

    // memberInfoDto
    memberInfoDto = MemberInfoDto.builder()
        .language(Language.KOREAN)
        .member(member)
        .build();
  }

  @Test
  void fixInfoReportWithWrongPost() {
    /**
     * GIVEN
     */
    InfoFixDto infoFixDto = new InfoFixDto();
    infoFixDto.setFixType(FixType.TIME.name());
    infoFixDto.setPostId(-1L);
    infoFixDto.setCategory(Category.FESTIVAL.name());
    infoFixDto.setContent("content");
    infoFixDto.setEmail("test@naver.com");

    /**
     * WHEN
     * THEN
     */
    assertThatThrownBy(
        () -> reportService.postInfoFixReport(memberInfoDto, infoFixDto, null)
    ).isInstanceOf(NotFoundException.class);
  }

  @Test
  void fixInfoReportWithNanaTest() {
    /**
     * GIVEN
     */
    Nana nana = Nana.builder()
        .version("1")
        .firstImageFile(imageFile1)
        .priority(0L)
        .build();
    em.persist(nana);

    NanaTitle nanaTitle = NanaTitle.builder()
        .heading("heading")
        .subHeading("subHeading")
        .language(Language.KOREAN)
        .nana(nana)
        .build();
    em.persist(nanaTitle);

    InfoFixDto infoFixDto = new InfoFixDto();
    infoFixDto.setFixType(FixType.CONTACT_OR_HOMEPAGE.name());
    infoFixDto.setPostId(nana.getId());
    infoFixDto.setCategory(Category.NANA.name());
    infoFixDto.setContent("content");
    infoFixDto.setEmail("test@naver.com");

    /**
     * WHEN
     * THEN
     */
    assertThatThrownBy(
        () -> reportService.postInfoFixReport(memberInfoDto, infoFixDto, null)
    ).isInstanceOf(BadRequestException.class);
  }
}