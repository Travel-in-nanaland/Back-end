package com.jeju.nanaland.domain.report.service;

import static com.jeju.nanaland.global.exception.ErrorCode.ALREADY_REPORTED;
import static com.jeju.nanaland.global.exception.ErrorCode.IMAGE_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.MAIL_FAIL_ERROR;
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
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.InfoFixReportImageFile;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportImageFile;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportVideoFile;
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
import com.jeju.nanaland.global.exception.ServerErrorException;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private static final int MAX_IMAGE_COUNT = 5;
  // TODO: 관리자 계정으로 바꾸기
  private static final String ADMIN_EMAIL = "jyajoo1020@gmail.com";
  private final MemberRepository memberRepository;
  private final ClaimReportVideoFileRepository claimReportVideoFileRepository;
  private final ClaimReportRepository claimReportRepository;
  private final ClaimReportImageFileRepository claimReportImageFileRepository;
  private final ReviewRepository reviewRepository;
  private final InfoFixReportRepository infoFixReportRepository;
  private final InfoFixReportImageFileRepository infoFixReportImageFileRepository;
  private final NatureRepository natureRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;
  private final ExperienceRepository experienceRepository;
  private final RestaurantRepository restaurantRepository;
  private final ImageFileService imageFileService;
  private final VideoFileService videoFileService;
  private final Environment env;
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;
  @Value("${cloud.aws.s3.infoFixReportImageDirectory}")
  private String INFO_FIX_REPORT_IMAGE_DIRECTORY;
  @Value("${cloud.aws.s3.claimReportFileDirectory}")
  private String CLAIM_REPORT_FILE_DIRECTORY;

  /**
   * 정보 수정 제안
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        수정 요청 DTO
   * @param files         수정 요청 이미지 파일 리스트
   * @throws NotFoundException 존재하는 게시물이 없는 경우
   */
  @Transactional
  public void requestPostInfoFix(MemberInfoDto memberInfoDto, ReportRequest.InfoFixDto reqDto,
      List<MultipartFile> files) {
    // 수정 요청 유효성 검사
    validateInfoFixReportRequest(reqDto, files);

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
    List<String> imageUrls = saveImagesAndGetUrls(files, infoFixReport,
        INFO_FIX_REPORT_IMAGE_DIRECTORY);

    // 이메일 전송
    sendEmailReport(memberInfoDto.getMember().getEmail(), infoFixReport, imageUrls);
  }

  /**
   * 정보 수정 제안 요청 유효성 확인
   *
   * @param reqDto 수정 요청 DTO
   * @param files  수정 요청 이미지 파일 리스트
   * @throws BadRequestException 카테고리가 올바르지 않은 경우
   */
  private void validateInfoFixReportRequest(InfoFixDto reqDto, List<MultipartFile> files) {
    Category category = Category.valueOf(reqDto.getCategory());

    // 나나스픽 전처리
    if (List.of(Category.NANA, Category.NANA_CONTENT).contains(category)) {
      throw new BadRequestException(NANA_INFO_FIX_FORBIDDEN.getMessage());
    }
    checkFileCountLimit(files);
  }

  /**
   * 파일 개수 유효성 확인
   *
   * @param files 파일 리스트
   * @throws BadRequestException 파일 개수가 초과된 경우
   */
  private void checkFileCountLimit(List<MultipartFile> files) {
    if (files != null && files.size() > MAX_IMAGE_COUNT) {
      throw new BadRequestException(IMAGE_BAD_REQUEST.getMessage());
    }
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
      case NATURE -> natureRepository.findCompositeDtoById(postId, language);
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
   * @param files         파일 리스트
   */
  @Transactional
  public void requestClaimReport(MemberInfoDto memberInfoDto, ReportRequest.ClaimReportDto reqDto,
      List<MultipartFile> files) {
    // 요청 유효성 확인
    validateClaimReportRequest(memberInfoDto, reqDto, files);

    // claimReport 저장
    ClaimReport claimReport = ClaimReport.builder()
        .member(memberInfoDto.getMember())
        .referenceId(reqDto.getId())
        .reportType(ReportType.valueOf(reqDto.getReportType()))
        .claimType(ClaimType.valueOf(reqDto.getClaimType()))
        .content(reqDto.getContent())
        .build();
    claimReportRepository.save(claimReport);

    // 이미지, 동영상 저장
    List<MultipartFile> imageFiles = filterFilesByType(files, "image/");
    List<MultipartFile> videoFiles = filterFilesByType(files, "video/");
    List<String> imageUrls = saveImagesAndGetUrls(imageFiles, claimReport,
        CLAIM_REPORT_FILE_DIRECTORY);
    List<String> videoUrls = saveVideosAndGetUrls(videoFiles, claimReport);

    // 이메일 전송
    List<String> combinedUrls = new ArrayList<>(imageUrls);
    combinedUrls.addAll(videoUrls);
    sendEmailReport(reqDto.getEmail(), claimReport, combinedUrls);
  }

  /**
   * 신고 요청 유효성 확인
   *
   * @param memberInfoDto 회원 정보
   * @param reqDto        신고 요청 DTO
   * @param files         파일 리스트
   * @throws BadRequestException 이미 신고한 적이 있는 경우
   */
  private void validateClaimReportRequest(MemberInfoDto memberInfoDto, ClaimReportDto reqDto,
      List<MultipartFile> files) {
    // 타입별 유효성 확인
    ReportType reportType = ReportType.valueOf(reqDto.getReportType());
    if (reportType == ReportType.REVIEW) {
      validateReviewReportRequest(memberInfoDto, reqDto);
    } else if (reportType == ReportType.MEMBER) {
      validateMemberReportRequest(memberInfoDto, reqDto);
    }

    // 이미 신고한 적이 있는지 확인
    Optional<ClaimReport> saveClaimReport = claimReportRepository.findByMemberAndReferenceIdAndReportType(
        memberInfoDto.getMember(), reqDto.getId(), ReportType.valueOf(reqDto.getReportType()));
    if (saveClaimReport.isPresent()) {
      throw new BadRequestException(ALREADY_REPORTED.getMessage());
    }

    checkFileCountLimit(files);
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
   * @param files 파일 리스트
   * @param type  파일 타입
   * @return 필터링된 파일 리스트
   */
  private List<MultipartFile> filterFilesByType(List<MultipartFile> files, String type) {
    if (files == null) {
      return new ArrayList<>();
    }
    return files.stream()
        .filter(file -> file.getContentType() != null && file.getContentType().startsWith(type))
        .collect(Collectors.toList());
  }

  /**
   * 이미지 파일 저장, 이미지 URL 리스트 얻기
   *
   * @param files     이미지 파일 리스트
   * @param report    요청 (InfoFixReport, ClaimReport)
   * @param directory 파일 저장 위치
   * @return 이미지 URL 리스트
   */
  private List<String> saveImagesAndGetUrls(List<MultipartFile> files, Object report,
      String directory) {
    if (files == null || files.isEmpty()) {
      return Collections.emptyList();
    }
    // 이미지 저장
    List<ImageFile> saveImageFiles = saveImages(files, directory);

    // 이미지와 Report 매핑 생성 및 저장
    List<Object> reportImageFiles = createReportImageFiles(saveImageFiles, report);
    if (report instanceof InfoFixReport) {
      saveInfoFixReportImageFiles(reportImageFiles);
    } else if (report instanceof ClaimReport) {
      saveClaimReportImages(reportImageFiles);
    }

    // 이미지 URL 리스트 반환
    return saveImageFiles.stream().map(ImageFile::getOriginUrl).collect(Collectors.toList());
  }

  /**
   * 이미지 파일 저장
   *
   * @param files     이미지 파일 리스트
   * @param directory 파일 저장 위치
   * @return 이미지 리스트
   */
  private List<ImageFile> saveImages(List<MultipartFile> files, String directory) {
    return files.stream()
        .map(file -> imageFileService.uploadAndSaveImageFile(file, false, directory))
        .collect(Collectors.toList());
  }

  /**
   * 이미지와 Report(InfoFixReport, ClaimReport)의 매핑 생성
   *
   * @param imageFiles 이미지 리스트
   * @param report     요청 (InfoFixReport, ClaimReport)
   * @return 이미지와 Report의 매핑 리스트
   * @throws IllegalArgumentException 지원하지 않는 타입인 경우
   */
  private List<Object> createReportImageFiles(List<ImageFile> imageFiles, Object report) {
    return imageFiles.stream()
        .map(imageFile -> {
          if (report instanceof InfoFixReport infoFixReport) {
            return InfoFixReportImageFile.builder()
                .imageFile(imageFile)
                .infoFixReport(infoFixReport)
                .build();
          } else if (report instanceof ClaimReport claimReport) {
            return ClaimReportImageFile.builder()
                .imageFile(imageFile)
                .claimReport(claimReport)
                .build();
          }
          throw new IllegalArgumentException("Unsupported report type");
        }).collect(Collectors.toList());
  }

  /**
   * 이미지와 InfoFixReport의 매핑 저장
   *
   * @param saveReportImageFiles 이미지와 InfoFixReport의 매핑 리스트
   */
  private void saveInfoFixReportImageFiles(List<Object> saveReportImageFiles) {
    // Object를 InfoFixReportImageFile로 변환
    List<InfoFixReportImageFile> infoFixReportImageFiles = saveReportImageFiles.stream()
        .map(o -> (InfoFixReportImageFile) o).collect(Collectors.toList());

    // 이미지와 InfoFixReport의 매핑 모두 저장
    infoFixReportImageFileRepository.saveAll(infoFixReportImageFiles);
  }

  /**
   * 이미지와 ClaimReport의 매핑 저장
   *
   * @param saveReportImageFiles 이미지와 ClaimReport의 매핑 리스트
   */
  private void saveClaimReportImages(List<Object> saveReportImageFiles) {
    // Object를 ClaimReportImageFile로 변환
    List<ClaimReportImageFile> claimReportImageFiles = saveReportImageFiles.stream()
        .map(o -> (ClaimReportImageFile) o).collect(Collectors.toList());

    // 이미지와 ClaimReport의 매핑 모두 저장
    claimReportImageFileRepository.saveAll(claimReportImageFiles);
  }

  /**
   * 동영상 파일 저장, 동영상 URL 리스트 얻기
   *
   * @param files  동영상 파일 리스트
   * @param report 요청 (InfoFixReport, ClaimReport)
   * @return 동영상 URL 리스트
   */
  private List<String> saveVideosAndGetUrls(List<MultipartFile> files, ClaimReport report) {
    if (files == null || files.isEmpty()) {
      return Collections.emptyList();
    }
    // 동영상 파일 저장
    List<VideoFile> saveVideoFiles = saveVideoFiles(files);

    // 동영상과 Report 매핑 생성 및 저장
    List<ClaimReportVideoFile> reportVideoFiles = createReportVideoFiles(saveVideoFiles, report);
    claimReportVideoFileRepository.saveAll(reportVideoFiles);

    // 동영상 URL 리스트 반환
    return saveVideoFiles.stream().map(VideoFile::getOriginUrl).collect(Collectors.toList());
  }

  /**
   * 동영상 파일 저장
   *
   * @param files 동영상 파일 리스트
   * @return 동영상 리스트
   */
  private List<VideoFile> saveVideoFiles(List<MultipartFile> files) {
    return files.stream()
        .map(file -> videoFileService.uploadAndSaveVideoFile(file, CLAIM_REPORT_FILE_DIRECTORY))
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

  /**
   * 메일 전송
   *
   * @param memberEmail 회원 이메일
   * @param report      요청 (InfoFixReport, ClaimReport)
   * @param urls        파일 URL 리스트
   * @throws ServerErrorException 메일 전송이 실패한 경우
   */
  private void sendEmailReport(String memberEmail, Object report, List<String> urls) {
    try {
      MimeMessage mimeMessage = createReportMail(memberEmail, report, urls);
      javaMailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ServerErrorException(MAIL_FAIL_ERROR.getMessage());
    }
  }

  /**
   * 메일 생성
   *
   * @param memberEmail 회원 이메일
   * @param report      요청 (InfoFixReport, ClaimReport)
   * @param urls        파일 URL 리스트
   * @return MimeMessage
   * @throws MessagingException           메일 관련 오류가 발생한 경우
   * @throws UnsupportedEncodingException 인코딩 오류가 발생한 경우
   */
  private MimeMessage createReportMail(String memberEmail, Object report, List<String> urls)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    String senderEmail = env.getProperty("spring.mail.username");
    message.setFrom(new InternetAddress(senderEmail, "Jeju in Nanaland"));
    message.setRecipients(RecipientType.TO, ADMIN_EMAIL);

    Context context = new Context();
    String templateName = "";
    if (report instanceof InfoFixReport infoFixReport) {
      setInfoFixReportContext(message, context, infoFixReport);
      templateName = "info-fix-report";
    } else if (report instanceof ClaimReport claimReport) {
      setClaimReportContext(memberEmail, message, context, claimReport);
      templateName = "claim-report";
    }

    for (int i = 0; i < urls.size(); i++) {
      context.setVariable("image_" + i, urls.get(i));
    }
    message.setText(templateEngine.process(templateName, context), "utf-8", "html");

    return message;
  }

  /**
   * 신고 요청 메일 내용 구성
   *
   * @param memberEmail 회원 이메일
   * @param message     내용
   * @param context     context
   * @param claimReport ClaimReport
   * @throws MessagingException 메일 관련 오류가 발생한 경우
   */
  private void setClaimReportContext(String memberEmail, MimeMessage message, Context context,
      ClaimReport claimReport) throws MessagingException {
    message.setSubject("[Nanaland] 리뷰 신고 요청입니다.");
    context.setVariable("report_type", claimReport.getReportType());
    context.setVariable("claim_type", claimReport.getClaimType());
    context.setVariable("id", claimReport.getId());
    context.setVariable("content", claimReport.getContent());
    context.setVariable("email", memberEmail);
  }

  /**
   * 정보 수정 제안 요청 메일 내용 구성
   *
   * @param message       내용
   * @param context       context
   * @param infoFixReport InfoFixReport
   * @throws MessagingException 메일 관련 오류가 발생한 경우
   */
  private void setInfoFixReportContext(MimeMessage message, Context context,
      InfoFixReport infoFixReport) throws MessagingException {
    message.setSubject("[Nanaland] 정보 수정 요청입니다.");
    context.setVariable("fix_type", infoFixReport.getFixType());
    context.setVariable("category", infoFixReport.getCategory());
    context.setVariable("language", infoFixReport.getLocale().name());
    context.setVariable("title", infoFixReport.getTitle());
    context.setVariable("content", infoFixReport.getContent());
    context.setVariable("email", infoFixReport.getEmail());
  }
}
