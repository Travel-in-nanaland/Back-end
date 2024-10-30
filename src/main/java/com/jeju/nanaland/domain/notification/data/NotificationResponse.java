package com.jeju.nanaland.domain.notification.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class NotificationResponse {

  @Getter
  @Builder
  @Schema(description = "알림 리스트 조회 결과")
  public static class NotificationListDto {

    @Schema(description = "총 알림 개수")
    private Long totalElements;

    @Schema(description = "알림 정보 리스트")
    private List<NotificationDetailDto> data;
  }

  @Getter
  @Builder
  @Schema(description = "알림 정보")
  public static class NotificationDetailDto {

    @Schema(description = "알림 id")
    private Long notificationId;

    @Schema(
        allowableValues = {
            "POST", "NOTICE", "NONE"
        },
        description = "POST - 나나스픽, 자연경관, 축제, 이색체험, 맛집 상세 페이지로 이동\n"
            + "NOTICE - 공지사항 상세 페이지로 이동\n"
            + "NONE - 페이지 이동 없음. 알림 정보만 제공"
    )
    private String clickEvent;

    @Schema(
        description = "상세 페이지 요청에 필요한 카테고리",
        allowableValues = {
            "NANA", "NATURE", "FESTIVAL", "MARKET", "FESTIVAL", "EXPERIENCE", "RESTAURANT",
            "NOTICE", "ETC"
        })
    private String category;

    @Schema(description = "상세 페이지 요청에 필요한 id")
    private Long contentId;

    @Schema(description = "알림 제목")
    private String title;

    @Schema(description = "알림 내용")
    private String content;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;
  }
}
