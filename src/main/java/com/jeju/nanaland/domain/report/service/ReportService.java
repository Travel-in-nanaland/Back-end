package com.jeju.nanaland.domain.report.service;

import static com.jeju.nanaland.global.exception.ErrorCode.ALREADY_REPORTED;
import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NANA_INFO_FIX_FORBIDDEN;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.SELF_REPORT_NOT_ALLOWED;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.VideoFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.common.service.MailService;
import com.jeju.nanaland.domain.common.service.VideoFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.report.dto.ReportRequest;
import com.jeju.nanaland.domain.report.dto.ReportRequest.ClaimReportDto;
import com.jeju.nanaland.domain.report.dto.ReportRequest.InfoFixDto;
import com.jeju.nanaland.domain.report.entity.infoFix.FixType;
import com.jeju.nanaland.domain.report.entity.infoFix.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.Report;
import com.jeju.nanaland.domain.report.entity.ReportStrategyFactory;
import com.jeju.nanaland.domain.report.entity.ReportStrategy;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportType;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportVideoFile;
import com.jeju.nanaland.domain.report.entity.claim.ClaimType;
import com.jeju.nanaland.domain.report.repository.ClaimReportRepository;
import com.jeju.nanaland.domain.report.repository.ClaimReportVideoFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.file.data.FileCategory;
import com.jeju.nanaland.global.file.service.FileUploadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private final MemberRepository memberRepository;
  private final ClaimReportVideoFileRepository claimReportVideoFileRepository;
  private final ClaimReportRepository claimReportRepository;
  private final ReviewRepository reviewRepository;
  private final InfoFixReportRepository infoFixReportRepository;
  private final NatureRepository natureRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;
  private final ExperienceRepository experienceRepository;
  private final RestaurantRepository restaurantRepository;
  private final ImageFileService imageFileService;
  private final VideoFileService videoFileService;
  private final MailService mailService;
  private final ReportStrategyFactory reportStrategyFactory;
  private final FileUploadService fileUploadService;

  /**
   * 정보 수정 제안
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        수정 요청 DTO
   * @throws NotFoundException 존재하는 게시물이 없는 경우
   */
  @Transactional
  public void requestPostInfoFix(MemberInfoDto memberInfoDto, ReportRequest.InfoFixDto reqDto) {
    // 수정 요청 유효성 검사
    validateInfoFixReportRequest(reqDto);

    // 해당 게시물 정보 가져오기
    CompositeDto compositeDto = findCompositeDto(Category.valueOf(reqDto.getCategory()),
        reqDto.getPostId(), memberInfoDto.getLanguage());
    if (compositeDto == null) {
      throw new NotFoundException(NOT_FOUND_EXCEPTION.getMessage());
    }

    // InfoFixReport 저장
    InfoFixReport infoFixReport = InfoFixReport.builder()
        .postId(reqDto.getPostId())
        .member(memberInfoDto.getMember())
        .category(Category.valueOf(reqDto.getCategory()))
        .fixType(FixType.valueOf(reqDto.getFixType()))
        .title(compositeDto.getTitle())
        .locale(memberInfoDto.getLanguage())
        .content(reqDto.getContent())
        .email(reqDto.getEmail())
        .build();
    infoFixReportRepository.save(infoFixReport);

    // 이미지 저장
    List<String> imageUrls = saveImagesAndGetUrls(reqDto.getFileKeys(), infoFixReport);

    // 이메일 전송
    mailService.sendEmailReport(infoFixReport, imageUrls);
  }

  /**
   * 정보 수정 제안 요청 유효성 확인
   *
   * @param reqDto 수정 요청 DTO
   * @throws BadRequestException 카테고리가 올바르지 않은 경우
   */
  private void validateInfoFixReportRequest(InfoFixDto reqDto) {
    Category category = Category.valueOf(reqDto.getCategory());

    // 나나스픽 전처리
    if (List.of(Category.NANA, Category.NANA_CONTENT).contains(category)) {
      throw new BadRequestException(NANA_INFO_FIX_FORBIDDEN.getMessage());
    }
    fileUploadService.validateFileKeys(reqDto.getFileKeys(), FileCategory.INFO_FIX_REPORT);
  }

  /**
   * 게시물 정보 조회
   *
   * @param category 카테고리
   * @param postId   게시물 ID
   * @param language 언어
   * @return 게시물 정보
   */
  private CompositeDto findCompositeDto(Category category, Long postId, Language language) {
    return switch (category) {
      case NATURE -> natureRepository.findNatureCompositeDto(postId, language);
      case MARKET -> marketRepository.findCompositeDtoById(postId, language);
      case FESTIVAL -> festivalRepository.findCompositeDtoById(postId, language);
      case EXPERIENCE -> experienceRepository.findCompositeDtoById(postId, language);
      case RESTAURANT -> restaurantRepository.findCompositeDtoById(postId, language);
      default -> null;
    };
  }

  /**
   * 신고 기능
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        신고 요청 DTO
   */
  @Transactional
  public void requestClaimReport(MemberInfoDto memberInfoDto, ReportRequest.ClaimReportDto reqDto) {
    // 요청 유효성 확인
    validateClaimReportRequest(memberInfoDto, reqDto);

    // claimReport 저장
    ClaimReport claimReport = ClaimReport.builder()
        .member(memberInfoDto.getMember())
        .referenceId(reqDto.getId())
        .claimReportType(ClaimReportType.valueOf(reqDto.getReportType()))
        .claimType(ClaimType.valueOf(reqDto.getClaimType()))
        .content(reqDto.getContent())
        .email(reqDto.getEmail())
        .build();
    claimReportRepository.save(claimReport);

    // 이미지, 동영상 저장
    List<String> imageFileKeys = filterFilesByType(reqDto.getFileKeys(), true);
    List<String> videoFileKeys = filterFilesByType(reqDto.getFileKeys(), false);

    List<String> imageUrls = saveImagesAndGetUrls(imageFileKeys, claimReport);
    List<String> videoUrls = saveVideosAndGetUrls(videoFileKeys, claimReport);

    List<String> combinedUrls = new ArrayList<>(imageUrls);
    combinedUrls.addAll(videoUrls);
    mailService.sendEmailReport(claimReport, combinedUrls);
  }

  /**
   * 신고 요청 유효성 확인
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        신고 요청 DTO
   * @throws BadRequestException 이미 신고한 적이 있는 경우
   */
  private void validateClaimReportRequest(MemberInfoDto memberInfoDto, ClaimReportDto reqDto) {
    // 타입별 유효성 확인
    ClaimReportType claimReportType = ClaimReportType.valueOf(reqDto.getReportType());
    if (claimReportType == ClaimReportType.REVIEW) {
      validateReviewReportRequest(memberInfoDto, reqDto);
    } else if (claimReportType == ClaimReportType.MEMBER) {
      validateMemberReportRequest(memberInfoDto, reqDto);
    }

    // 이미 신고한 적이 있는지 확인
    Optional<ClaimReport> saveClaimReport = claimReportRepository.findByMemberAndReferenceIdAndClaimReportType(
        memberInfoDto.getMember(), reqDto.getId(), ClaimReportType.valueOf(reqDto.getReportType()));
    if (saveClaimReport.isPresent()) {
      throw new BadRequestException(ALREADY_REPORTED.getMessage());
    }

    fileUploadService.validateFileKeys(reqDto.getFileKeys(), FileCategory.CLAIM_REPORT);
  }

  /**
   * 리뷰 신고 요청 유효성 확인
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        신고 요청 DTO
   * @throws NotFoundException   존재하는 리뷰가 없는 경우
   * @throws BadRequestException 본인이 리뷰를 작성한 경우
   */
  private void validateReviewReportRequest(MemberInfoDto memberInfoDto,
      ReportRequest.ClaimReportDto reqDto) {
    Review review = reviewRepository.findById(reqDto.getId())
        .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND.getMessage()));

    // 본인이 작성한 것인지 확인
    if (review.getMember().equals(memberInfoDto.getMember())) {
      throw new BadRequestException(SELF_REPORT_NOT_ALLOWED.getMessage());
    }
  }

  /**
   * 유저 신고 요청 유효성 확인
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        신고 요청 DTO
   * @throws BadRequestException 본인을 신고하는 경우
   * @throws NotFoundException   존재하는 회원이 없는 경우
   */
  private void validateMemberReportRequest(MemberInfoDto memberInfoDto,
      ReportRequest.ClaimReportDto reqDto) {
    if (memberInfoDto.getMember().getId().equals(reqDto.getId())) {
      throw new BadRequestException(SELF_REPORT_NOT_ALLOWED.getMessage());
    }
    memberRepository.findById(reqDto.getId())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
  }

  /**
   * 타입별 파일 필터링
   *
   * @param fileKeys 파일 키 리스트
   * @param isImage  파일 타입 (이미지이면 true, 영상이면 false)
   * @return 필터링된 파일 리스트
   */
  private List<String> filterFilesByType(List<String> fileKeys, boolean isImage) {
    if (fileKeys == null) {
      return new ArrayList<>();
    }
    return fileKeys.stream()
        .filter(fileKey ->
            (isImage && FileCategory.CLAIM_REPORT.isImage(fileKey))
                || (!isImage && FileCategory.CLAIM_REPORT.isVideo(fileKey)))
        .collect(Collectors.toList());
  }

  /**
   * 이미지 파일 저장, 이미지 URL 리스트 얻기
   *
   * @param fileKeys   파일 키 리스트
   * @param report    요청 (InfoFixReport, ClaimReport)
   * @return 이미지 URL 리스트
   */
  private List<String> saveImagesAndGetUrls(List<String> fileKeys, Report report) {
    if (fileKeys == null || fileKeys.isEmpty()) {
      return new ArrayList<>();
    }
    // 이미지 저장
    List<ImageFile> imageFiles = fileKeys.stream()
        .map(imageFileService::getAndSaveImageFile)
        .toList();

    ReportStrategy reportStrategy = reportStrategyFactory.findStrategy(report.getReportType());
    reportStrategy.saveReportImages(report, imageFiles);
    return imageFiles.stream()
        .map(ImageFile::getOriginUrl)
        .collect(Collectors.toList());
  }

  /**
   * 동영상 파일 저장, 동영상 URL 리스트 얻기
   *
   * @param fileKeys  동영상 파일 키 리스트
   * @param report 요청 (InfoFixReport, ClaimReport)
   * @return 동영상 URL 리스트
   */
  private List<String> saveVideosAndGetUrls(List<String> fileKeys, ClaimReport report) {
    if (fileKeys == null || fileKeys.isEmpty()) {
      return new ArrayList<>();
    }

    // 동영상 파일 저장
    List<VideoFile> videoFiles = fileKeys.stream()
        .map(videoFileService::getAndSaveVideoFile)
        .toList();
    List<ClaimReportVideoFile> reportVideoFiles = createReportVideoFiles(videoFiles, report);
    claimReportVideoFileRepository.saveAll(reportVideoFiles);

    // 동영상 URL 리스트 반환
    return videoFiles.stream()
        .map(VideoFile::getOriginUrl)
        .collect(Collectors.toList());
  }

  /**
   * 동영상과 ClaimReport의 매핑 저장
   *
   * @param videoFiles  동영상 리스트
   * @param claimReport ClaimReport
   * @return 동영상과 ClaimReport의 매핑 리스트
   */
  private List<ClaimReportVideoFile> createReportVideoFiles(List<VideoFile> videoFiles,
      ClaimReport claimReport) {
    return videoFiles.stream()
        .map(videoFile -> ClaimReportVideoFile.builder()
            .videoFile(videoFile)
            .claimReport(claimReport)
            .build())
        .collect(Collectors.toList());
  }
}
