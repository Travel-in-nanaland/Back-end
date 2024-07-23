package com.jeju.nanaland.domain.notice.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
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

    private String noticeCategory;
    private String title;
    private LocalDateTime createdAt;

    @QueryProjection
    public NoticeTitleDto(String noticeCategory, String title, LocalDateTime createdAt) {
      this.noticeCategory = noticeCategory;
      this.title = title;
      this.createdAt = createdAt;
    }
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class NoticeDetailDto {

    private String title;
    private LocalDateTime createdAt;

    private List<NoticeContentDto> noticeContents;

    @QueryProjection
    public NoticeDetailDto(String title, LocalDateTime createdAt) {
      this.title = title;
      this.createdAt = createdAt;
    }
  }

  @Data
  @Builder
  public static class NoticeContentDto {

    private ImageFileDto imageFileDto;
    private String content;

    @QueryProjection
    public NoticeContentDto(ImageFileDto imageFileDto, String content) {
      if (imageFileDto.getThumbnailUrl() == null && imageFileDto.getOriginUrl() == null) {
        imageFileDto = null;
      }
      this.imageFileDto = imageFileDto;
      this.content = content;
    }
  }
}
