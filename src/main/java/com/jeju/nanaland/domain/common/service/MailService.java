package com.jeju.nanaland.domain.common.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MAIL_FAIL_ERROR;

import com.jeju.nanaland.domain.report.entity.Report;
import com.jeju.nanaland.global.exception.ServerErrorException;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

  private static final String ADMIN_EMAIL = "travel.in.nanaland@gmail.com";
  private final Environment env;
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  /**
   * 메일 전송
   *
   * @param report      요청 (InfoFixReport, ClaimReport)
   * @param urls        파일 URL 리스트
   * @throws ServerErrorException 메일 전송이 실패한 경우
   */
  @Async("mailExecutor")
  public void sendEmailReport(Report report, List<String> urls) {
    try {
      MimeMessage mimeMessage = createReportMail(report, urls);
      javaMailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error("이메일 전송 오류 : {}", e.getMessage());
      throw new ServerErrorException(MAIL_FAIL_ERROR.getMessage());
    }
  }

  /**
   * 메일 생성
   *
   * @param report      요청 (InfoFixReport, ClaimReport)
   * @param urls        파일 URL 리스트
   * @return MimeMessage
   * @throws MessagingException           메일 관련 오류가 발생한 경우
   * @throws UnsupportedEncodingException 인코딩 오류가 발생한 경우
   */
  private MimeMessage createReportMail(Report report, List<String> urls)
      throws MessagingException, UnsupportedEncodingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    String senderEmail = env.getProperty("spring.mail.username");
    message.setFrom(new InternetAddress(senderEmail, "Jeju in Nanaland"));
    message.setRecipients(RecipientType.TO, ADMIN_EMAIL);

    Context context = new Context();
    String templateName = report.setReportContextAndGetTemplate(message, context);

    for (int i = 0; i < urls.size(); i++) {
      context.setVariable("image_" + i, urls.get(i));
    }
    message.setText(templateEngine.process(templateName, context), "utf-8", "html");

    return message;
  }
}
