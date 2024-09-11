package com.jeju.nanaland.domain.common.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MAIL_FAIL_ERROR;

import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
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
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

  // TODO: 관리자 계정으로 바꾸기
  private static final String ADMIN_EMAIL = "jyajoo1020@gmail.com";
  private final Environment env;
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  /**
   * 메일 전송
   *
   * @param memberEmail 회원 이메일
   * @param report      요청 (InfoFixReport, ClaimReport)
   * @param urls        파일 URL 리스트
   * @throws ServerErrorException 메일 전송이 실패한 경우
   */
  public void sendEmailReport(String memberEmail, Object report, List<String> urls) {
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
