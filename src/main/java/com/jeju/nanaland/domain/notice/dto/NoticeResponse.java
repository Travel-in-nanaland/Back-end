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
  @Schema(name = "NoticePreviewPageDto", description = "공지사항 전체 리스트 조회 DTO")
  public static class PreviewPageDto {

    @Schema(description = "총 조회 개수")
    private Long totalElements;

    @Schema(description = "결과 데이터")
    private List<PreviewDto> data;
  }

  @Data
  @Builder
  @Schema(name = "NoticePreviewDto", description = "공지사항 프리뷰 정보 DTO")
  public static class PreviewDto {

    @Schema(description = "공지사항 id")
    private Long id;
    @Schema(description = "카테고리")
    private String noticeCategory;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @QueryProjection
    public PreviewDto(Long id, String noticeCategory, String title, LocalDateTime createdAt) {
      this.id = id;
      this.noticeCategory = noticeCategory;
      this.title = title;
      this.createdAt = createdAt;
    }
  }

  @Data
  @Builder
  @AllArgsConstructor
  @Schema(name = "NoticeDetailDto", description = "공지사항 상세 DTO")
  public static class DetailDto {

    @Schema(description = "제목")
    private String title;
    @Schema(description = "생성일")
    private LocalDate createdAt;
    @Schema(description = "내용 리스트")
    private List<ContentDto> noticeContents;

    @QueryProjection
    public DetailDto(String title, LocalDateTime createdAt) {
      this.title = title;
      this.createdAt = createdAt.toLocalDate();
    }
  }

  @Data
  @Builder
  @Schema(name = "NoticeContentDto", description = "공지사항 내용 DTO")
  public static class ContentDto {

    @Schema(description = "이미지")
    private ImageFileDto image;
    @Schema(description = "내용")
    private String content;

    @QueryProjection
    public ContentDto(ImageFileDto imageFileDto, String content) {
      if (imageFileDto.getThumbnailUrl() == null && imageFileDto.getOriginUrl() == null) {
        imageFileDto = null;
      }
      this.image = imageFileDto;
      this.content = content;
    }
  }
}
