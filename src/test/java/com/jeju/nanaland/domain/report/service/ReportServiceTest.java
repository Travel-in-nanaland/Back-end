package com.jeju.nanaland.domain.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.entity.Nana;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import com.jeju.nanaland.domain.report.dto.ReportRequest.InfoFixDto;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.util.TestUtil;
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
    imageFile1 = TestUtil.findImageFileByNumber(em, 1);

    // language
    language = Language.KOREAN;

    // member
    member = TestUtil.findMemberByLanguage(em, language, 1);

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
    Nana nana = TestUtil.findNana(em, 1);

    NanaTitle nanaTitle = TestUtil.findNanaTitleByNana(em, nana);

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