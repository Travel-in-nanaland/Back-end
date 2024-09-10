package com.jeju.nanaland.domain.report.service;

import static com.jeju.nanaland.global.exception.ErrorCode.IMAGE_BAD_REQUEST;
import static com.jeju.nanaland.global.exception.ErrorCode.MAIL_FAIL_ERROR;
import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.NANA_INFO_FIX_FORBIDDEN;
import static com.jeju.nanaland.global.exception.ErrorCode.NOT_FOUND_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.REVIEW_ALREADY_REPORTED;
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

  @Transactional
  public void requestPostInfoFix(MemberInfoDto memberInfoDto, ReportRequest.InfoFixDto reqDto,
      List<MultipartFile> images) {
    // 수정 요청 유효성 검사
    validateInfoFixReportRequest(reqDto, images);

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
    List<String> imageUrlList = saveImagesAndGetUrls(images, infoFixReport,
        INFO_FIX_REPORT_IMAGE_DIRECTORY);

    // 이메일 전송
    sendEmailReport(memberInfoDto.getMember().getEmail(), infoFixReport, imageUrlList);
  }

  private void validateInfoFixReportRequest(InfoFixDto reqDto, List<MultipartFile> images) {
    Category category = Category.valueOf(reqDto.getCategory());

    // 나나스픽 전처리
    if (List.of(Category.NANA, Category.NANA_CONTENT).contains(category)) {
      throw new BadRequestException(NANA_INFO_FIX_FORBIDDEN.getMessage());
    }
    checkFileCountLimit(images);
  }

  private void checkFileCountLimit(List<MultipartFile> files) {
    if (files != null && files.size() > MAX_IMAGE_COUNT) {
      throw new BadRequestException(IMAGE_BAD_REQUEST.getMessage());
    }
  }

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

  @Transactional
  public void requestClaimReport(MemberInfoDto memberInfoDto, ReportRequest.ClaimReportDto reqDto,
      List<MultipartFile> fileList) {
    // 요청 유효성 확인
    validateClaimReportRequest(memberInfoDto, reqDto, fileList);

    // claimReport 저장
    ClaimReport claimReport = ClaimReport.builder()
        .member(memberInfoDto.getMember())
        .referenceId(reqDto.getId())
        .reportType(ReportType.valueOf(reqDto.getReportType()))
        .claimType(ClaimType.valueOf(reqDto.getClaimType()))
        .content(reqDto.getContent())
        .build();
    claimReportRepository.save(claimReport);

    // 이미지, 동영상 분리
    List<String> imageUrlList = processAndSaveFiles(fileList, claimReport, "image/");
    List<String> videoUrlList = processAndSaveFiles(fileList, claimReport, "video/");

    // 이메일 전송
    List<String> combinedUrlList = new ArrayList<>(imageUrlList);
    combinedUrlList.addAll(videoUrlList);
    sendEmailReport(reqDto.getEmail(), claimReport, combinedUrlList);
  }

  private void validateClaimReportRequest(MemberInfoDto memberInfoDto, ClaimReportDto reqDto,
      List<MultipartFile> fileList) {
    // 타입별 유효성 확인
    ReportType reportType = ReportType.valueOf(reqDto.getReportType());
    if (reportType == ReportType.REVIEW) {
      validateReviewReportRequest(memberInfoDto, reqDto);
    } else if (reportType == ReportType.MEMBER) {
      validateMemberReportRequest(memberInfoDto, reqDto);
    }

    // 이미 신고한 적이 있는지 확인
    Optional<ClaimReport> claimReportOptional = claimReportRepository.findByMemberAndIdAndReportType(
        memberInfoDto.getMember(), reqDto.getId(), ReportType.valueOf(reqDto.getReportType()));

    if (claimReportOptional.isPresent()) {
      throw new BadRequestException(REVIEW_ALREADY_REPORTED.getMessage());
    }
    checkFileCountLimit(fileList);
  }

  private void validateReviewReportRequest(MemberInfoDto memberInfoDto,
      ReportRequest.ClaimReportDto reqDto) {
    Review review = reviewRepository.findById(reqDto.getId())
        .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND.getMessage()));
    if (review.getMember().equals(memberInfoDto.getMember())) {
      throw new BadRequestException(SELF_REPORT_NOT_ALLOWED.getMessage());
    }
  }

  private void validateMemberReportRequest(MemberInfoDto memberInfoDto,
      ReportRequest.ClaimReportDto reqDto) {
    if (memberInfoDto.getMember().getId().equals(reqDto.getId())) {
      throw new BadRequestException(SELF_REPORT_NOT_ALLOWED.getMessage());
    }
    memberRepository.findById(reqDto.getId())
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
  }

  private List<String> processAndSaveFiles(List<MultipartFile> fileList, ClaimReport claimReport,
      String type) {
    if (type.equals("image/")) {
      List<MultipartFile> imageFiles = filterFilesByType(fileList, type);
      return saveImagesAndGetUrls(imageFiles, claimReport, CLAIM_REPORT_FILE_DIRECTORY);
    } else if (type.equals("video/")) {
      List<MultipartFile> videoFiles = filterFilesByType(fileList, type);
      return saveVideosAndGetUrls(videoFiles, claimReport);
    }
    throw new IllegalArgumentException("Unsupported file type");
  }

  private List<MultipartFile> filterFilesByType(List<MultipartFile> fileList, String type) {
    if (fileList == null) {
      return new ArrayList<>();
    }
    return fileList.stream()
        .filter(file -> file.getContentType() != null && file.getContentType().startsWith(type))
        .collect(Collectors.toList());
  }

  private List<String> saveImagesAndGetUrls(List<MultipartFile> imageList, Object report,
      String directory) {
    if (imageList == null || imageList.isEmpty()) {
      return Collections.emptyList();
    }
    // 이미지 저장
    List<ImageFile> saveImageFiles = saveImages(imageList, directory);

    // 이미지와 객체 연관 관계 생성 및 저장
    List<Object> reportImageFiles = createReportImageFiles(saveImageFiles, report);
    if (report instanceof InfoFixReport) {
      saveInfoFixReportImageFiles(reportImageFiles);
    } else if (report instanceof ClaimReport) {
      saveClaimReportImages(reportImageFiles);
    }

    // 이미지 URL 리스트 반환
    return saveImageFiles.stream().map(ImageFile::getOriginUrl).collect(Collectors.toList());
  }

  private List<ImageFile> saveImages(List<MultipartFile> imageFiles, String directory) {
    return imageFiles.stream()
        .map(image -> imageFileService.uploadAndSaveImageFile(image, false, directory))
        .collect(Collectors.toList());
  }

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
        })
        .collect(Collectors.toList());
  }

  private void saveInfoFixReportImageFiles(List<Object> saveImageFiles) {
    // Object를 InfoFixReportImageFile로 변환
    List<InfoFixReportImageFile> infoFixReportImageFiles = saveImageFiles.stream()
        .map(o -> (InfoFixReportImageFile) o).collect(Collectors.toList());

    // InfoFixReportImageFile(연관관계)를 모두 저장
    infoFixReportImageFileRepository.saveAll(infoFixReportImageFiles);
  }

  private void saveClaimReportImages(List<Object> saveImageFiles) {
    // Object를 ClaimReportImageFile로 변환
    List<ClaimReportImageFile> claimReportImageFiles = saveImageFiles.stream()
        .map(o -> (ClaimReportImageFile) o).collect(Collectors.toList());

    // ClaimReportImageFile(연관관계)를 모두 저장
    claimReportImageFileRepository.saveAll(claimReportImageFiles);
  }

  private List<String> saveVideosAndGetUrls(List<MultipartFile> videoFiles, ClaimReport report) {
    if (videoFiles == null || videoFiles.isEmpty()) {
      return Collections.emptyList();
    }
    // 동영상 파일 저장
    List<VideoFile> saveVideoFiles = saveVideoFiles(videoFiles);

    // 동영상 연관 관계 저장 && url 반환
    List<ClaimReportVideoFile> reportVideoFiles = createReportVideoFiles(saveVideoFiles, report);
    claimReportVideoFileRepository.saveAll(reportVideoFiles);

    return saveVideoFiles.stream().map(VideoFile::getOriginUrl).collect(Collectors.toList());
  }

  private List<VideoFile> saveVideoFiles(List<MultipartFile> videoFiles) {
    return videoFiles.stream()
        .map(video -> videoFileService.uploadAndSaveVideoFile(video, CLAIM_REPORT_FILE_DIRECTORY))
        .collect(Collectors.toList());
  }

  private List<ClaimReportVideoFile> createReportVideoFiles(List<VideoFile> saveVideoFiles,
      ClaimReport claimReport) {
    return saveVideoFiles.stream()
        .map(videoFile -> ClaimReportVideoFile.builder()
            .videoFile(videoFile)
            .claimReport(claimReport)
            .build())
        .collect(Collectors.toList());
  }

  private void sendEmailReport(String email, Object report, List<String> imageUrlList) {
    try {
      MimeMessage mimeMessage = createReportMail(email, report, imageUrlList);
      javaMailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ServerErrorException(MAIL_FAIL_ERROR.getMessage());
    }
  }

  private MimeMessage createReportMail(String memberEmail, Object report, List<String> imageUrlList)
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

    for (int i = 0; i < imageUrlList.size(); i++) {
      context.setVariable("image_" + i, imageUrlList.get(i));
    }
    message.setText(templateEngine.process(templateName, context), "utf-8", "html");

    return message;
  }

  private void setClaimReportContext(String memberEmail, MimeMessage message, Context context,
      ClaimReport claimReport) throws MessagingException {
    message.setSubject("[Nanaland] 리뷰 신고 요청입니다.");
    context.setVariable("report_type", claimReport.getReportType());
    context.setVariable("claim_type", claimReport.getClaimType());
    context.setVariable("id", claimReport.getId());
    context.setVariable("content", claimReport.getContent());
    context.setVariable("email", memberEmail);
  }

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
