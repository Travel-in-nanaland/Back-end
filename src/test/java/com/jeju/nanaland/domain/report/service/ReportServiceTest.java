package com.jeju.nanaland.domain.report.service;

import static com.jeju.nanaland.global.exception.ErrorCode.IMAGE_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_ALREADY_REPORTED;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.SELF_REPORT_NOT_ALLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.VideoFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.VideoFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.report.dto.ReportRequest.ClaimReportDto;
import com.jeju.nanaland.domain.report.dto.ReportRequest.InfoFixDto;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimType;
import com.jeju.nanaland.domain.report.entity.claim.ReportType;
import com.jeju.nanaland.domain.report.repository.ClaimReportImageFileRepository;
import com.jeju.nanaland.domain.report.repository.ClaimReportRepository;
import com.jeju.nanaland.domain.report.repository.ClaimReportVideoFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportImageFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
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
  ReviewRepository reviewRepository;
  @Mock
  ClaimReportRepository claimReportRepository;
  @Mock
  ClaimReportImageFileRepository claimReportImageFileRepository;
  @Mock
  ClaimReportVideoFileRepository claimReportVideoFileRepository;
  @Mock
  ImageFileService imageFileService;
  @Mock
  VideoFileService videoFileService;
  @Mock
  Environment env;
  @Mock
  JavaMailSender javaMailSender;
  @Mock
  SpringTemplateEngine templateEngine;

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

  private Review createReview(Member member) {
    return Review.builder()
        .category(Category.EXPERIENCE)
        .member(member)
        .content("content")
        .build();
  }

  private ClaimReportDto createClaimReportDto(ReportType reportType) {
    return ClaimReportDto.builder()
        .id(1L)
        .email("test@gmail.com")
        .reportType(reportType.name())
        .claimType(ClaimType.ETC.name())
        .build();
  }

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
        () -> reportService.requestPostInfoFix(memberInfoDto, infoFixDto, new ArrayList<>())
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
        () -> reportService.requestPostInfoFix(memberInfoDto, infoFixDto, new ArrayList<>())
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
        .findNatureCompositeDto(1L, Language.KOREAN);
    doReturn(mock(ImageFile.class)).when(imageFileService)
        .uploadAndSaveImageFile(any(MultipartFile.class), eq(false), any());
    doReturn(mock(MimeMessage.class)).when(javaMailSender).createMimeMessage();
    doReturn("nanaland.jeju@gmail.com").when(env).getProperty("spring.mail.username");
    doReturn(null).when(infoFixReportRepository).save(any(InfoFixReport.class));

    // when
    // then
    reportService.requestPostInfoFix(memberInfoDto, infoFixDto, List.of(mock(MultipartFile.class)));
  }

  @Test
  @DisplayName("리뷰 신고 실패 - 리뷰가 존재하지 않는 경우")
  void requestClaimReportDtoFail() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    ClaimReportDto claimReportDto = createClaimReportDto(ReportType.REVIEW);
    doReturn(Optional.empty()).when(reviewRepository).findById(any());

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(REVIEW_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("리뷰 신고 실패 - 파일 개수가 최대개수를 넘는 경우")
  void requestClaimReportDtoFail2() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    MemberInfoDto memberInfoDto2 = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    ClaimReportDto claimReportDto = createClaimReportDto(ReportType.REVIEW);
    Review review = createReview(memberInfoDto2.getMember());
    List<MultipartFile> fileList = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      fileList.add(new MockMultipartFile("image", "test.png", "png", "test file".getBytes(
          StandardCharsets.UTF_8)));
    }
    doReturn(Optional.of(review)).when(reviewRepository).findById(any());

    // when
    BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, fileList));

    // then
    assertThat(badRequestException.getMessage()).isEqualTo(IMAGE_BAD_REQUEST.getMessage());
  }

  @Test
  @DisplayName("리뷰 신고 실패 - 본인의 리뷰 신고")
  void requestClaimReportDtoFail3() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    ClaimReportDto claimReportDto = createClaimReportDto(ReportType.REVIEW);
    Review review = createReview(memberInfoDto.getMember());
    doReturn(Optional.of(review)).when(reviewRepository).findById(any());

    // when
    BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null));

    // then
    assertThat(badRequestException.getMessage()).isEqualTo(SELF_REPORT_NOT_ALLOWED.getMessage());
  }

  @Test
  @DisplayName("리뷰 신고 실패 - 이미 신고한 리뷰인 경우")
  void requestClaimReportDtoFail4() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    MemberInfoDto memberInfoDto2 = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    ClaimReportDto claimReportDto = createClaimReportDto(ReportType.REVIEW);
    Review review = createReview(memberInfoDto2.getMember());
    doReturn(Optional.of(review)).when(reviewRepository).findById(any());
    doReturn(Optional.of(claimReportDto)).when(claimReportRepository)
        .findByMemberAndIdAndReportType(any(), any(), any());

    // when
    BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null));

    // then
    assertThat(badRequestException.getMessage()).isEqualTo(REVIEW_ALREADY_REPORTED.getMessage());
  }

  @Test
  @DisplayName("리뷰 신고 성공")
  void requestClaimReportSuccess() {
    // given
    MemberInfoDto memberInfoDto = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    MemberInfoDto memberInfoDto2 = createMemberInfoDto(Language.KOREAN, TravelType.NONE);
    ClaimReportDto claimReportDto = createClaimReportDto(ReportType.REVIEW);
    Review review = createReview(memberInfoDto2.getMember());
    List<MultipartFile> fileList = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      fileList.add(new MockMultipartFile("image", "test.png", "image/png", "test file".getBytes(
          StandardCharsets.UTF_8)));
      fileList.add(new MockMultipartFile("video", "test.mp4", "video/mp4", "test file".getBytes(
          StandardCharsets.UTF_8)));
    }
    doReturn(Optional.of(review)).when(reviewRepository).findById(any());
    doReturn(mock(MimeMessage.class)).when(javaMailSender).createMimeMessage();
    doReturn("nanaland.jeju@gmail.com").when(env).getProperty("spring.mail.username");

    // Mock image and video file service methods
    ImageFile mockImageFile = mock(ImageFile.class);
    VideoFile mockVideoFile = mock(VideoFile.class);

    doReturn(mockImageFile).when(imageFileService)
        .uploadAndSaveImageFile(any(), anyBoolean(), any());
    doReturn(mockVideoFile).when(videoFileService).uploadAndSaveVideoFile(any(), any());

    // Mock URL returns
    doReturn("imageUrl").when(mockImageFile).getOriginUrl();
    doReturn("videoUrl").when(mockVideoFile).getOriginUrl();

    // when
    reportService.requestClaimReport(memberInfoDto, claimReportDto, fileList);

    // then
    verify(claimReportRepository, times(1)).save(any(ClaimReport.class));
    verify(imageFileService, times(2)).uploadAndSaveImageFile(any(), anyBoolean(), any());
    verify(videoFileService, times(2)).uploadAndSaveVideoFile(any(), any());
    verify(claimReportImageFileRepository, times(1)).saveAll(anyList());
    verify(claimReportVideoFileRepository, times(1)).saveAll(anyList());
    verify(javaMailSender, times(1)).send(any(MimeMessage.class));
  }
}