package com.jeju.nanaland.domain.report.service;

import static com.jeju.nanaland.global.exception.ErrorCode.ALREADY_REPORTED;
import static com.jeju.nanaland.global.exception.ErrorCode.IMAGE_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NANA_INFO_FIX_FORBIDDEN;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.SELF_REPORT_NOT_ALLOWED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.VideoFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.MailService;
import com.jeju.nanaland.domain.common.service.VideoFileService;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.report.dto.ReportRequest;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.Report;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportType;
import com.jeju.nanaland.domain.report.entity.claim.ClaimType;
import com.jeju.nanaland.domain.report.repository.ClaimReportImageFileRepository;
import com.jeju.nanaland.domain.report.repository.ClaimReportRepository;
import com.jeju.nanaland.domain.report.repository.ClaimReportVideoFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportImageFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
  MarketRepository marketRepository;
  @Mock
  ReviewRepository reviewRepository;
  @Mock
  ClaimReportRepository claimReportRepository;
  @Mock
  ClaimReportImageFileRepository claimReportImageFileRepository;
  @Mock
  ClaimReportVideoFileRepository claimReportVideoFileRepository;
  @Mock
  MemberRepository memberRepository;
  @Mock
  ImageFileService imageFileService;
  @Mock
  VideoFileService videoFileService;
  @Mock
  MailService mailService;
  MemberInfoDto memberInfoDto, memberInfoDto2;

  private static List<MultipartFile> createImageMultipartFiles(int itemCount) {
    List<MultipartFile> files = new ArrayList<>();
    for (int i = 0; i < itemCount; i++) {
      files.add(new MockMultipartFile("image", "test.png", "image/png",
          "test file".getBytes(StandardCharsets.UTF_8)));
    }
    return files;
  }

  private static List<MultipartFile> createVideoMultipartFiles(int itemCount) {
    List<MultipartFile> files = new ArrayList<>();
    for (int i = 0; i < itemCount; i++) {
      files.add(new MockMultipartFile("video", "test.mp4", "video/mp4",
          "test file".getBytes(StandardCharsets.UTF_8)));
    }
    return files;
  }

  @BeforeEach
  void setUp() {
    memberInfoDto = createMemberInfoDto();
    memberInfoDto2 = createMemberInfoDto();
  }

  private MemberInfoDto createMemberInfoDto() {
    Member member = spy(Member.builder()
        .language(Language.KOREAN)
        .travelType(TravelType.NONE)
        .build());

    return MemberInfoDto.builder()
        .member(member)
        .language(Language.KOREAN)
        .build();
  }

  private ReportRequest.InfoFixDto createInfoFixDto(Category category) {
    return ReportRequest.InfoFixDto.builder()
        .fixType(FixType.CONTACT_OR_HOMEPAGE.name())
        .email("test@naver.com")
        .content("content")
        .category(category.name())
        .postId(1L)
        .build();
  }

  private Review createReview(Member member) {
    return Review.builder()
        .category(Category.EXPERIENCE)
        .member(member)
        .content("content")
        .build();
  }

  private ReportRequest.ClaimReportDto createClaimReportDto(ClaimReportType claimReportType) {
    return ReportRequest.ClaimReportDto.builder()
        .id(1L)
        .email("test@gmail.com")
        .reportType(claimReportType.name())
        .claimType(ClaimType.ETC.name())
        .build();
  }

  @Nested
  @DisplayName("정보 수정 제안 TEST")
  class RequestPostInfoFix {

    @ParameterizedTest
    @DisplayName("실패 - 카테고리가 올바르지 않은 경우")
    @EnumSource(value = Category.class, names = {"NANA", "NANA_CONTENT"}, mode = Mode.INCLUDE)
    void requestPostInfoFixFail_invalidCategory(Category category) {
      // given: 나나스픽에 대한 정보 수정 제안 요청 설정
      ReportRequest.InfoFixDto infoFixDto = createInfoFixDto(category);

      // when: 정보 수정 제안
      // then: ErrorCode 검증
      assertThatThrownBy(() -> reportService.requestPostInfoFix(memberInfoDto, infoFixDto, null))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(NANA_INFO_FIX_FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("실패 - 파일 개수가 초과된 경우")
    void requestPostInfoFixFail_fileCountOverLimit() {
      // given: 파일 개수가 초과되도록 설정
      ReportRequest.InfoFixDto infoFixDto = createInfoFixDto(Category.MARKET);
      List<MultipartFile> files = createImageMultipartFiles(6);

      // when: 정보 수정 제안
      // then: ErrorCode 검증
      assertThatThrownBy(() -> reportService.requestPostInfoFix(memberInfoDto, infoFixDto, files))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(IMAGE_BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재하는 게시물이 없는 경우")
    void requestPostInfoFixFail_postNotFound() {
      // given: 파일 개수가 초과되도록 설정
      ReportRequest.InfoFixDto infoFixDto = createInfoFixDto(Category.MARKET);
      doReturn(null).when(marketRepository)
          .findCompositeDtoById(any(), any(Language.class));

      // when: 정보 수정 제안
      // then: ErrorCode 검증
      assertThatThrownBy(() -> reportService.requestPostInfoFix(memberInfoDto, infoFixDto, null))
          .isInstanceOf(NotFoundException.class)
          .hasMessage(NOT_FOUND_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("성공")
    void requestPostInfoFixSuccess() {
      // given: 정보 수정 제안 요청 설정
      ReportRequest.InfoFixDto infoFixDto = createInfoFixDto(Category.MARKET);
      int itemCount = 3;
      List<MultipartFile> files = createImageMultipartFiles(itemCount);

      doReturn(MarketCompositeDto.builder().build()).when(marketRepository)
          .findCompositeDtoById(any(), any(Language.class));
      doReturn(null).when(infoFixReportRepository).save(any(InfoFixReport.class));
      doReturn(mock(ImageFile.class)).when(imageFileService)
          .uploadAndSaveImageFile(any(MultipartFile.class), eq(false), any());

      // when: 정보 수정 제안
      reportService.requestPostInfoFix(memberInfoDto, infoFixDto, files);

      // then: 정보 수정 제안 요청 검증
      verify(infoFixReportRepository).save(any(InfoFixReport.class));
      verify(imageFileService, times(itemCount)).uploadAndSaveImageFile(any(MultipartFile.class),
          eq(false), any());
      verify(infoFixReportImageFileRepository).saveAll(anyList());
      verify(mailService).sendEmailReport(any(Report.class), any());
    }
  }

  @Nested
  @DisplayName("신고 요청 TEST")
  class RequestClaimReport {

    @Test
    @DisplayName("실패 - 리뷰가 존재하지 않는 경우")
    void requestClaimReportFail_reviewNotFound() {
      // given: 존재하는 리뷰가 없도록 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.REVIEW);
      doReturn(Optional.empty()).when(reviewRepository).findById(any());

      // when: 리뷰 신고 요청
      // then: ErrorCode 검증
      assertThatThrownBy(
          () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null))
          .isInstanceOf(NotFoundException.class)
          .hasMessage(REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 본인이 작성한 리뷰를 신고한 경우")
    void requestClaimReportFail_selfReportedReview() {
      // given: 본인이 작성한 리뷰에 대해 신고 요청하도록 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.REVIEW);
      Review review = createReview(memberInfoDto.getMember());
      doReturn(Optional.of(review)).when(reviewRepository).findById(any());

      // when: 리뷰 신고 요청
      // then: ErrorCode 검증
      assertThatThrownBy(
          () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(SELF_REPORT_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("실패 - 이미 신고한 경우")
    void requestClaimReportFail_alreadyReported() {
      // given: 이미 신고한 적이 있도록 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.REVIEW);
      Review review = createReview(memberInfoDto2.getMember());
      doReturn(Optional.of(review)).when(reviewRepository).findById(any());
      doReturn(Optional.of(claimReportDto)).when(claimReportRepository)
          .findByMemberAndReferenceIdAndClaimReportType(any(Member.class), any(), any(ClaimReportType.class));

      // when: 리뷰 신고 요청
      // then: ErrorCode 검증
      assertThatThrownBy(
          () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(ALREADY_REPORTED.getMessage());
    }

    @Test
    @DisplayName("실패 - 파일 개수가 초과된 경우")
    void requestClaimReportFail_fileCountOverLimit() {
      // given: 파일 개수가 초과되도록 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.REVIEW);
      Review review = createReview(memberInfoDto2.getMember());
      List<MultipartFile> files = createImageMultipartFiles(6);
      doReturn(Optional.of(review)).when(reviewRepository).findById(any());
      doReturn(Optional.empty()).when(claimReportRepository)
          .findByMemberAndReferenceIdAndClaimReportType(any(Member.class), any(), any(ClaimReportType.class));

      // when: 리뷰 신고 요청
      // then: ErrorCode 검증
      assertThatThrownBy(
          () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, files))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(IMAGE_BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("리뷰 신고 성공")
    void requestReviewClaimReportSuccess() {
      // given: 리뷰 신고 요청 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.REVIEW);
      Review review = createReview(memberInfoDto2.getMember());
      int imageCount = 3;
      int videoCount = 2;
      List<MultipartFile> imageMultipartFiles = createImageMultipartFiles(imageCount);
      List<MultipartFile> videoMultipartFiles = createVideoMultipartFiles(videoCount);
      List<MultipartFile> files = new ArrayList<>(imageMultipartFiles);
      files.addAll(videoMultipartFiles);

      doReturn(Optional.of(review)).when(reviewRepository).findById(any());
      doReturn(Optional.empty()).when(claimReportRepository)
          .findByMemberAndReferenceIdAndClaimReportType(any(Member.class), any(), any(ClaimReportType.class));
      doReturn(mock(ImageFile.class)).when(imageFileService)
          .uploadAndSaveImageFile(any(MultipartFile.class), eq(false), any());
      doReturn(mock(VideoFile.class)).when(videoFileService)
          .uploadAndSaveVideoFile(any(MultipartFile.class), any());

      // when: 리뷰 신고 요청
      reportService.requestClaimReport(memberInfoDto, claimReportDto, files);

      // then: 리뷰 신고 요청 검증
      verify(claimReportRepository).save(any(ClaimReport.class));
      verify(imageFileService, times(imageCount)).uploadAndSaveImageFile(any(), anyBoolean(),
          any());
      verify(videoFileService, times(videoCount)).uploadAndSaveVideoFile(any(), any());
      verify(claimReportImageFileRepository).saveAll(anyList());
      verify(claimReportVideoFileRepository).saveAll(anyList());
      verify(mailService).sendEmailReport(any(Report.class), any());
    }

    @Test
    @DisplayName("실패 - 본인을 신고하는 경우")
    void requestClaimReportFail_selfReportedMember() {
      // given: 본인에 대해 신고 요청하도록 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.MEMBER);
      doReturn(claimReportDto.getId()).when(memberInfoDto.getMember()).getId();

      // when: 유저 신고 요청
      // then: ErrorCode 검증
      assertThatThrownBy(
          () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(SELF_REPORT_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재하는 회원이 없는 경우")
    void requestClaimReportFail_memberNotFound() {
      // given: 존재하는 회원이 없도록 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.MEMBER);
      doReturn(2L).when(memberInfoDto.getMember()).getId();
      doReturn(Optional.empty()).when(memberRepository).findById(any());

      // when: 유저 신고 요청
      // then: ErrorCode 검증
      assertThatThrownBy(
          () -> reportService.requestClaimReport(memberInfoDto, claimReportDto, null))
          .isInstanceOf(NotFoundException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("유저 신고 성공")
    void requestMemberClaimReportSuccess() {
      // given: 유저 신고 요청 설정
      ReportRequest.ClaimReportDto claimReportDto = createClaimReportDto(ClaimReportType.MEMBER);
      int imageCount = 3;
      int videoCount = 2;
      List<MultipartFile> imageMultipartFiles = createImageMultipartFiles(imageCount);
      List<MultipartFile> videoMultipartFiles = createVideoMultipartFiles(videoCount);
      List<MultipartFile> files = new ArrayList<>(imageMultipartFiles);
      files.addAll(videoMultipartFiles);

      doReturn(2L).when(memberInfoDto.getMember()).getId();
      doReturn(Optional.of(memberInfoDto2.getMember())).when(memberRepository).findById(any());
      doReturn(Optional.empty()).when(claimReportRepository)
          .findByMemberAndReferenceIdAndClaimReportType(any(Member.class), any(), any(ClaimReportType.class));
      doReturn(mock(ImageFile.class)).when(imageFileService)
          .uploadAndSaveImageFile(any(MultipartFile.class), eq(false), any());
      doReturn(mock(VideoFile.class)).when(videoFileService)
          .uploadAndSaveVideoFile(any(MultipartFile.class), any());

      // when: 유저 신고 요청
      reportService.requestClaimReport(memberInfoDto, claimReportDto, files);

      // then: 유저 신고 요청 검증
      verify(claimReportRepository).save(any(ClaimReport.class));
      verify(imageFileService, times(imageCount)).uploadAndSaveImageFile(any(), anyBoolean(),
          any());
      verify(videoFileService, times(videoCount)).uploadAndSaveVideoFile(any(), any());
      verify(claimReportImageFileRepository).saveAll(anyList());
      verify(claimReportVideoFileRepository).saveAll(anyList());
      verify(mailService).sendEmailReport(any(Report.class), any());
    }
  }
}