package com.jeju.nanaland.domain.report.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.report.dto.ReportRequest;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import com.sun.jdi.InternalException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
  private final NatureRepository natureRepository;
  private final MarketRepository marketRepository;
  private final FestivalRepository festivalRepository;
  private final ExperienceRepository experienceRepository;

  private final S3ImageService s3ImageService;
  private final Environment env;
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  private final String INFO_FIX_REPORT_IMAGE_DIRECTORY = "/info_fix_report_images";

  @Transactional
  public void postInfoFixReport(MemberInfoDto memberInfoDto, ReportRequest.InfoFixDto reqDto,
      MultipartFile multipartFile) {

    if (reqDto.getCategory().equals(Category.NANA.name())) {
      throw new BadRequestException("나나스픽 게시물은 정보 수정 요청이 불가능합니다.");
    }

    Long postId = reqDto.getPostId();
    Language locale = memberInfoDto.getLanguage();
    String title = null;
    switch (Category.valueOf(reqDto.getCategory())) {
      case NATURE -> {
        NatureCompositeDto compositeDto = natureRepository.findCompositeDtoById(postId, locale);
        if (compositeDto == null) {
          throw new NotFoundException("해당 7대자연 게시물이 없습니다.");
        }
        title = compositeDto.getTitle();
      }
      case MARKET -> {
        MarketCompositeDto compositeDto = marketRepository.findCompositeDtoById(postId, locale);
        if (compositeDto == null) {
          throw new NotFoundException("해당 전통시장 게시물이 없습니다.");
        }
        title = compositeDto.getTitle();
      }
      case FESTIVAL -> {
        FestivalCompositeDto compositeDto = festivalRepository.findCompositeDtoById(postId, locale);
        if (compositeDto == null) {
          throw new NotFoundException("해당 축제 게시물이 없습니다.");
        }
        title = compositeDto.getTitle();
      }
      case EXPERIENCE -> {
        ExperienceCompositeDto compositeDto = experienceRepository.findCompositeDtoById(postId,
            locale);
        if (compositeDto == null) {
          throw new NotFoundException("해당 이색체험 게시물이 없습니다.");
        }
        title = compositeDto.getTitle();
      }
    }

    String imageUrl = null;
    if (multipartFile != null) {
      try {
        S3ImageDto s3ImageDto = s3ImageService.uploadImageToS3(multipartFile, false,
            INFO_FIX_REPORT_IMAGE_DIRECTORY);
        imageUrl = s3ImageDto.getOriginUrl();
      } catch (IOException e) {
        throw new InternalException("이미지 업로드 실패");
      }
    }

    Category categoryContent = Category.valueOf(reqDto.getCategory());
    FixType fixType = FixType.valueOf(reqDto.getFixType());

    InfoFixReport infoFixReport = InfoFixReport.builder()
        .postId(reqDto.getPostId())
        .member(memberInfoDto.getMember())
        .category(categoryContent)
        .fixType(fixType)
        .title(title)
        .locale(memberInfoDto.getLanguage())
        .content(reqDto.getContent())
        .email(reqDto.getEmail())
        .imageUrl(imageUrl)
        .build();

    try {
      MimeMessage mimeMessage = createInfoFixReportMail("travel.in.nanaland@gmail.com",
          infoFixReport);
      javaMailSender.send(mimeMessage);
      infoFixReportRepository.save(infoFixReport);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new ServerErrorException("메일 전송 실패");
    }
  }

  private MimeMessage createInfoFixReportMail(String mail, InfoFixReport infoFixReport)
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
    context.setVariable("image", infoFixReport.getImageUrl());
    context.setVariable("content", infoFixReport.getContent());
    context.setVariable("email", infoFixReport.getEmail());
    message.setText(templateEngine.process("info-fix-report", context), "utf-8", "html");

    return message;
  }
}
