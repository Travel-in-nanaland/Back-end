package com.jeju.nanaland.global.component;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Setter
public class SlackErrorLogAppender extends AppenderBase<ILoggingEvent> {

  // webhookurl, 슬랙메시지에 찍힐 서버 돌아가는 환경 xml에서 yml 값 불러와서 저장
  private String url;

  private String env;

  @Override
  protected void append(final ILoggingEvent eventObject) {

    final RestTemplate restTemplate = new RestTemplate();
    final Map<String, Object> body = createSlackErrorBody(eventObject);
    restTemplate.postForEntity(url, body, String.class);
  }

  private Map<String, Object> createSlackErrorBody(final ILoggingEvent eventObject) {
    final String message = createMessage(eventObject);
    return Map.of(
        "username", "윤아 석희 태호 비상",
        "icon_emoji", ":shocked_face_with_exploding_head",
        "attachments", List.of(
            Map.of(
                "fallback", ":rotating_light: 에러 발생 :rotating_light:",
                "color", "#2eb886",
                "pretext", "에러가 발생했어요 확인해주세요 :cry:",
                "author_name", "Error 발생",
                "text", message,
                "fields", List.of(
                    Map.of(
                        "title", "서버 환경",
                        "value", env,
                        "short", false
                    )
                ),
                "ts", eventObject.getTimeStamp()
            )
        )
    );
  }

  private String createMessage(final ILoggingEvent eventObject) {
    final String baseMessage = "에러가 발생했습니다.\n";
    final String pattern = baseMessage + "```%s %s %s [%s] - %s```";
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    return String.format(pattern,
        simpleDateFormat.format(eventObject.getTimeStamp()),
        eventObject.getLevel(),
        eventObject.getThreadName(),
        eventObject.getLoggerName(),
        eventObject.getFormattedMessage());
  }
}