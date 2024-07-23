package com.jeju.nanaland.domain.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.report.dto.ReportRequest.InfoFixDto;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.InfoFixReportImageFile;
import com.jeju.nanaland.domain.report.repository.InfoFixReportImageFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @InjectMocks
  ReportService reportService;

  @Mock
  InfoFixReportRepository infoFixReportRepository;
  @Mock
  InfoFixReportImageFileRepository infoFixReportImageFileRepository;
  @Mock
  NatureRepository natureRepository;
  @Mock
  MarketRepository marketRepository;
  @Mock
  FestivalRepository festivalRepository;
  @Mock
  ExperienceRepository experienceRepository;
  @Mock
  RestaurantRepository restaurantRepository;
  @Mock
  ImageFileService imageFileService;
  @Mock
  Environment env;
  @Mock
  JavaMailSender javaMailSender;
  @Mock
  SpringTemplateEngine templateEngine;

  @Test
  @DisplayName("없는 게시물에 대한 요청")
  void fixInfoReportWithWrongPost() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    InfoFixDto infoFixDto = InfoFixDto.builder()
        .fixType(FixType.CONTACT_OR_HOMEPAGE.name())
        .postId(1L)
        .email("test@naver.com")
        .category(Category.MARKET.name())
        .content("수정 내용")
        .build();
    doReturn(null).when(marketRepository).findCompositeDtoById(1L, Language.KOREAN);

    // when
    // then
    assertThatThrownBy(
        () -> reportService.postInfoFixReport(memberInfoDto, infoFixDto, new ArrayList<>())
    ).isInstanceOf(NotFoundException.class);
  }

  @ParameterizedTest
  @DisplayName("나나스픽 정보 수정 제안 요청")
  @EnumSource(value = Category.class, names = {"NANA", "NANA_CONTENT"}, mode = Mode.INCLUDE)
  void fixInfoReportWithNanaTest(Category category) {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    InfoFixDto infoFixDto = InfoFixDto.builder()
        .fixType(FixType.CONTACT_OR_HOMEPAGE.name())
        .postId(1L)
        .email("test@naver.com")
        .category(category.name())
        .content("수정 내용")
        .build();

    // when
    // then
    assertThatThrownBy(
        () -> reportService.postInfoFixReport(memberInfoDto, infoFixDto, new ArrayList<>())
    ).isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("정보 수정 제안 요청")
  void fixInfoReportTest() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    InfoFixDto infoFixDto = InfoFixDto.builder()
        .fixType(FixType.CONTACT_OR_HOMEPAGE.name())
        .postId(1L)
        .email("test@naver.com")
        .category(Category.NATURE.name())
        .content("수정 내용")
        .build();

    doReturn(NatureCompositeDto.builder().build()).when(natureRepository)
        .findCompositeDtoById(1L, Language.KOREAN);
    doReturn(mock(ImageFile.class)).when(imageFileService)
        .uploadAndSaveImageFile(any(MultipartFile.class), eq(false), eq("/info_fix_report_images"));
    doReturn(null).when(infoFixReportImageFileRepository).save(any(InfoFixReportImageFile.class));
    doReturn(mock(MimeMessage.class)).when(javaMailSender).createMimeMessage();
    doReturn("nanaland.jeju@gmail.com").when(env).getProperty("spring.mail.username");
    doReturn(null).when(infoFixReportRepository).save(any(InfoFixReport.class));

    // when
    // then
    reportService.postInfoFixReport(memberInfoDto, infoFixDto, List.of(mock(MultipartFile.class)));
  }


  private MemberInfoDto createMemberInfoDto(Language language, TravelType travelType) {
    Member member = Member.builder()
        .language(language)
        .travelType(travelType)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }
}