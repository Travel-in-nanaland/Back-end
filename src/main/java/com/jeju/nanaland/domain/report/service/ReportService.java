package com.jeju.nanaland.domain.report.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.service.ImageFileService;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.report.dto.ReportRequest;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.InfoFixReportImageFile;
import com.jeju.nanaland.domain.report.repository.InfoFixReportImageFileRepository;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final InfoFixReportRepository infoFixReportRepository;
  private final InfoFixReportImageFileRepository infoFixReportImageFileRepository;
  private final NatureRepository natureRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;
  private final ExperienceRepository experienceRepository;
  private final RestaurantRepository restaurantRepository;

  private final ImageFileService imageFileService;
  private final Environment env;
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  private final String INFO_FIX_REPORT_IMAGE_DIRECTORY = "/info_fix_report_images";

  @Transactional
  public void postInfoFixReport(MemberInfoDto memberInfoDto, ReportRequest.InfoFixDto reqDto,
      List<MultipartFile> imageList) {
    // 나나스픽 전처리
    if (reqDto.getCategory().equals(Category.NANA.name())) {
      throw new BadRequestException("나나스픽 게시물은 정보 수정 요청이 불가능합니다.");
    }

    // 이미지 5장 이상 전처리
    if (imageList.size() > 5) {
      throw new BadRequestException("이미지는 최대 5장까지 요청 가능합니다.");
    }

    // 해당 게시물 정보 가져오기
    Long postId = reqDto.getPostId();
    Category category = Category.valueOf(reqDto.getCategory());
    Language language = memberInfoDto.getLanguage();
    CompositeDto compositeDto = findCompositeDto(category, postId, language);
    if (compositeDto == null) {
      throw new NotFoundException("해당 축제 게시물이 없습니다.");
    }

    // InfoFixReport 엔티티 객체 생성
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

    // 이미지 저장
    List<String> imageUrlList = new ArrayList<>();
    if (imageList != null) {
      for (MultipartFile image : imageList) {
        // S3에 저장, ImageFile 객체 생성
        ImageFile imageFile = imageFileService.uploadAndSaveImageFile(image, false,
            INFO_FIX_REPORT_IMAGE_DIRECTORY);
        imageUrlList.add(imageFile.getOriginUrl());

        InfoFixReportImageFile infoFixReportImageFile = InfoFixReportImageFile.builder()
            .imageFile(imageFile)
            .infoFixReport(infoFixReport)
            .build();

        // 정보 수정 제안 이미지 저장
        infoFixReportImageFileRepository.save(infoFixReportImageFile);
      }
    }

    // 관리자 메일로 전송
    // TODO: 배포시 관리자 메일로 변경하기
    try {
//      MimeMessage mimeMessage = createInfoFixReportMail("travel.in.nanaland@gmail.com",
//          infoFixReport, imageUrlList);
      MimeMessage mimeMessage = createInfoFixReportMail("skdlzl753@naver.com",
          infoFixReport, imageUrlList);
      javaMailSender.send(mimeMessage);
      infoFixReportRepository.save(infoFixReport);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ServerErrorException("메일 전송 실패");
    }
  }

  private MimeMessage createInfoFixReportMail(String mail, InfoFixReport infoFixReport,
      List<String> imageUrlList)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    String senderEmail = env.getProperty("spring.mail.username");
    message.setFrom(new InternetAddress(senderEmail, "Jeju in Nanaland"));
    message.setRecipients(MimeMessage.RecipientType.TO, mail);
    message.setSubject("[Nanaland] 정보 수정 요청입니다.");

    Context context = new Context();
    context.setVariable("fix_type", infoFixReport.getFixType());
    context.setVariable("category", infoFixReport.getCategory());
    context.setVariable("language", infoFixReport.getLocale().name());
    context.setVariable("title", infoFixReport.getTitle());
    context.setVariable("content", infoFixReport.getContent());
    context.setVariable("email", infoFixReport.getEmail());
    for (int i = 0; i < imageUrlList.size(); i++) {
      context.setVariable("image_" + i, imageUrlList.get(i));
    }

    message.setText(templateEngine.process("info-fix-report", context), "utf-8", "html");

    return message;
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
}
