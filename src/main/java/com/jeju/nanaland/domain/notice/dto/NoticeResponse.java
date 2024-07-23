package com.jeju.nanaland.domain.notice.dto;

import com.jeju.nanaland.domain.notice.entity.NoticeCategory;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class NoticeResponse {

  @Data
  @Builder
  @Schema(description = "공지사항 전체 리스트 조회 DTO")
  public static class NoticeListDto {

    @Schema(description = "총 조회 개수")
    private Long totalElements;

    @Schema(description = "결과 데이터")
    private List<NoticeTitleDto> data;
  }

  @Data
  @Builder
  public static class NoticeTitleDto {

    private NoticeCategory noticeCategory;
    private String title;
    private LocalDateTime createdAt;

    @QueryProjection
    public NoticeTitleDto(NoticeCategory noticeCategory, String title, LocalDateTime createdAt) {
      this.noticeCategory = noticeCategory;
      this.title = title;
      this.createdAt = createdAt;
    }
  }
}
