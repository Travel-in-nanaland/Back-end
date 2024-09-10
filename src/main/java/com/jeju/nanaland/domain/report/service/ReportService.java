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
  @Value("${cloud.aws.s3.infoFixReportImageDirectory}")
  private String INFO_FIX_REPORT_IMAGE_DIRECTORY;
  @Value("${cloud.aws.s3.claimReportFileDirectory}")
  private String CLAIM_REPORT_FILE_DIRECTORY;
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

  // 정보 수정 제안
  @Transactional
  public void requestPostInfoFix(MemberInfoDto memberInfoDto, ReportRequest.InfoFixDto reqDto,
      List<MultipartFile> imageList) {

    Category category = Category.valueOf(reqDto.getCategory());
    // 나나스픽 전처리
    if (List.of(Category.NANA, Category.NANA_CONTENT).contains(category)) {
      throw new BadRequestException(NANA_INFO_FIX_FORBIDDEN.getMessage());
    }

    // 이미지 5장 이상 전처리
    if (imageList != null && imageList.size() > MAX_IMAGE_COUNT) {
      throw new BadRequestException(IMAGE_BAD_REQUEST.getMessage());
    }

    // 해당 게시물 정보 가져오기
    Long postId = reqDto.getPostId();
    Language language = memberInfoDto.getLanguage();
    CompositeDto compositeDto = findCompositeDto(category, postId, language);
    if (compositeDto == null) {
      throw new NotFoundException(NOT_FOUND_EXCEPTION.getMessage());
    }

    // InfoFixReport 저장
    String title = compositeDto.getTitle();
    FixType fixType = FixType.valueOf(reqDto.getFixType());
    InfoFixReport infoFixReport = InfoFixReport.builder()
        .postId(reqDto.getPostId())
        .member(memberInfoDto.getMember())
        .category(category)
        .fixType(fixType)
        .title(title)
        .locale(memberInfoDto.getLanguage())
        .content(reqDto.getContent())
        .email(reqDto.getEmail())
        .build();
    infoFixReportRepository.save(infoFixReport);

    // 이미지 저장
    List<String> imageUrlList = saveImagesAndGetUrls(imageList, infoFixReport,
        INFO_FIX_REPORT_IMAGE_DIRECTORY);

    // 이메일 전송
    sendEmailReport(memberInfoDto.getMember().getEmail(), infoFixReport, imageUrlList);
  }

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

  // 신고 기능
  @Transactional
  public void requestClaimReport(MemberInfoDto memberInfoDto, ClaimReportDto reqDto,
      List<MultipartFile> fileList) {
    validateReportRequest(memberInfoDto, reqDto);
    checkExistingReport(memberInfoDto, reqDto);

    // 파일 개수 확인
    if (fileList != null && fileList.size() > MAX_IMAGE_COUNT) {
      throw new BadRequestException(IMAGE_BAD_REQUEST.getMessage());
    }

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
    List<MultipartFile> imageFiles = new ArrayList<>();
    List<MultipartFile> videoFiles = new ArrayList<>();
    if (fileList != null) {
      for (MultipartFile file : fileList) {
        String contentType = file.getContentType();
        if (contentType != null) {
          if (contentType.startsWith("image/")) {
            imageFiles.add(file);
          } else if (contentType.startsWith("video/")) {
            videoFiles.add(file);
          }
        }
      }
    }

    // claimReportImageFile 이미지 저장
    List<String> imageUrlList = saveImagesAndGetUrls(imageFiles, claimReport,
        CLAIM_REPORT_FILE_DIRECTORY);

    // claimReportVideoFile 동영상 저장
    List<String> videoUrlList = saveVideosAndGetUrls(videoFiles, claimReport);

    // 이메일 전송
    List<String> combinedUrlList = new ArrayList<>(imageUrlList);
    combinedUrlList.addAll(videoUrlList);
    sendEmailReport(reqDto.getEmail(), claimReport, combinedUrlList);
  }

  // 상황별 요청 유효 확인
  private void validateReportRequest(MemberInfoDto memberInfoDto, ClaimReportDto reqDto) {
    ReportType reportType = ReportType.valueOf(reqDto.getReportType());
    // 리뷰를 신고하는 경우 - 리뷰 데이터 조회, 본인이 작성한 것인지 확인
    if (reportType == ReportType.REVIEW) {
      Review review = reviewRepository.findById(reqDto.getId())
          .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND.getMessage()));
      if (review.getMember().equals(memberInfoDto.getMember())) {
        throw new BadRequestException(SELF_REPORT_NOT_ALLOWED.getMessage());
      }
    }
    // 유저를 신고하는 경우 - 본인을 신고하는 것인지 확인, 유저 데이터 조회
    else if (reportType == ReportType.MEMBER) {
      if (memberInfoDto.getMember().getId().equals(reqDto.getId())) {
        throw new BadRequestException(SELF_REPORT_NOT_ALLOWED.getMessage());
      }
      memberRepository.findById(reqDto.getId())
          .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    }
  }

  // 이미 신고한 적이 있는지 확인
  private void checkExistingReport(MemberInfoDto memberInfoDto, ClaimReportDto reqDto) {
    Optional<ClaimReport> claimReportOptional = claimReportRepository.findByMemberAndIdAndReportType(
        memberInfoDto.getMember(), reqDto.getId(), ReportType.valueOf(reqDto.getReportType()));

    if (claimReportOptional.isPresent()) {
      throw new BadRequestException(REVIEW_ALREADY_REPORTED.getMessage());
    }
  }

  // 이미지 저장
  private List<String> saveImagesAndGetUrls(List<MultipartFile> imageList, Object report,
      String directory) {
    List<String> imageUrlList = new ArrayList<>();
    if (imageList != null) {
      // 이미지 파일 저장
      List<Object> saveImageFiles = saveImageFiles(imageList, report, directory);

      // 이미지 연관 관계 저장 && url 반환
      if (report instanceof InfoFixReport) {
        List<InfoFixReportImageFile> infoFixReportImageFiles = saveImageFiles.stream()
            .map(o -> (InfoFixReportImageFile) o).collect(Collectors.toList());
        infoFixReportImageFileRepository.saveAll(infoFixReportImageFiles);
        imageUrlList = infoFixReportImageFiles.stream()
            .map(file -> file.getImageFile().getOriginUrl())
            .collect(Collectors.toList());
      } else if (report instanceof ClaimReport) {
        List<ClaimReportImageFile> claimReportImageFiles = saveImageFiles.stream()
            .map(o -> (ClaimReportImageFile) o).collect(Collectors.toList());
        claimReportImageFileRepository.saveAll(claimReportImageFiles);
        imageUrlList = claimReportImageFiles.stream()
            .map(file -> file.getImageFile().getOriginUrl())
            .collect(Collectors.toList());
      }
    }
    return imageUrlList;
  }

  // 이미지 파일 저장 && 이미지 연관 관계 객체 생성
  private List<Object> saveImageFiles(List<MultipartFile> imageList, Object report,
      String directory) {
    List<Object> imageFileList = new ArrayList<>();
    for (MultipartFile image : imageList) {
      ImageFile imageFile = imageFileService.uploadAndSaveImageFile(image, false, directory);

      if (report instanceof InfoFixReport infoFixReport) {
        imageFileList.add(InfoFixReportImageFile.builder()
            .imageFile(imageFile)
            .infoFixReport(infoFixReport)
            .build());
      } else if (report instanceof ClaimReport claimReport) {
        imageFileList.add(ClaimReportImageFile.builder()
            .imageFile(imageFile)
            .claimReport(claimReport)
            .build());
      }
    }
    return imageFileList;
  }

  // 동영상 저장
  private List<String> saveVideosAndGetUrls(List<MultipartFile> videoFiles, ClaimReport report) {
    List<String> videoUrlList = new ArrayList<>();
    if (videoFiles != null) {
      // 동영상 파일 저장
      List<ClaimReportVideoFile> claimReportVideoFiles = saveVideoFiles(videoFiles, report);

      // 동영상 연관 관계 저장 && url 반환
      claimReportVideoFileRepository.saveAll(claimReportVideoFiles);
      videoUrlList = claimReportVideoFiles.stream()
          .map(file -> file.getVideoFile().getOriginUrl())
          .collect(Collectors.toList());
    }
    return videoUrlList;
  }

  // 동영상 파일 저장 && 동영상 연관 관계 생성
  private List<ClaimReportVideoFile> saveVideoFiles(List<MultipartFile> videoFiles,
      ClaimReport report) {
    List<ClaimReportVideoFile> videoFileList = new ArrayList<>();
    for (MultipartFile video : videoFiles) {
      VideoFile videoFile = videoFileService.uploadAndSaveVideoFile(video,
          CLAIM_REPORT_FILE_DIRECTORY);

      videoFileList.add(ClaimReportVideoFile.builder()
          .videoFile(videoFile)
          .claimReport(report)
          .build());
    }
    return videoFileList;
  }

  // 메일 전송
  private void sendEmailReport(String email, Object report, List<String> imageUrlList) {
    try {
      MimeMessage mimeMessage = createReportMail(email, report, imageUrlList);
      javaMailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ServerErrorException(MAIL_FAIL_ERROR.getMessage());
    }
  }

  // 메일 생성
  private MimeMessage createReportMail(String memberEmail, Object report, List<String> imageUrlList)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    String senderEmail = env.getProperty("spring.mail.username");
    message.setFrom(new InternetAddress(senderEmail, "Jeju in Nanaland"));
    message.setRecipients(RecipientType.TO, ADMIN_EMAIL);

    Context context = new Context();
    String templateName = "";
    if (report instanceof InfoFixReport infoFixReport) {
      message.setSubject("[Nanaland] 정보 수정 요청입니다.");
      context.setVariable("fix_type", infoFixReport.getFixType());
      context.setVariable("category", infoFixReport.getCategory());
      context.setVariable("language", infoFixReport.getLocale().name());
      context.setVariable("title", infoFixReport.getTitle());
      context.setVariable("content", infoFixReport.getContent());
      context.setVariable("email", infoFixReport.getEmail());
      templateName = "info-fix-report";
    } else if (report instanceof ClaimReport claimReport) {
      message.setSubject("[Nanaland] 리뷰 신고 요청입니다.");
      context.setVariable("report_type", claimReport.getReportType());
      context.setVariable("claim_type", claimReport.getClaimType());
      context.setVariable("id", claimReport.getId());
      context.setVariable("content", claimReport.getContent());
      context.setVariable("email", memberEmail);
      templateName = "claim-report";
    }

    for (int i = 0; i < imageUrlList.size(); i++) {
      context.setVariable("image_" + i, imageUrlList.get(i));
    }

    message.setText(templateEngine.process(templateName, context), "utf-8", "html");

    return message;
  }
}
