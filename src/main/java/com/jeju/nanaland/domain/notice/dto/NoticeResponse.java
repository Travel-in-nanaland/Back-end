package com.jeju.nanaland.domain.notice.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
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

    @Schema(description = "카테고리")
    private String noticeCategory;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "생성일")
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

    @Schema(description = "제목")
    private String title;
    @Schema(description = "생성일")
    private LocalDate createdAt;
    @Schema(description = "내용 리스트")
    private List<NoticeContentDto> noticeContents;

    @QueryProjection
    public NoticeDetailDto(String title, LocalDateTime createdAt) {
      this.title = title;
      this.createdAt = createdAt.toLocalDate();
    }
  }

  @Data
  @Builder
  public static class NoticeContentDto {

    @Schema(description = "이미지")
    private ImageFileDto image;
    @Schema(description = "내용")
    private String content;

    @QueryProjection
    public NoticeContentDto(ImageFileDto imageFileDto, String content) {
      if (imageFileDto.getThumbnailUrl() == null && imageFileDto.getOriginUrl() == null) {
        imageFileDto = null;
      }
      this.image = imageFileDto;
      this.content = content;
    }
  }
}
