package com.jeju.nanaland.domain.nature.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class NatureResponse {

  @Getter
  @Builder
  @Schema(name = "NaturePreviewPageDto", description = "7대 자연 전체 리스트 조회 DTO")
  public static class PreviewPageDto {

    @Schema(description = "7대 자연 전체 게시물 수")
    private Long totalElements;

    @Schema(description = "7대 자연 게시물 결과 리스트")
    private List<PreviewDto> data;

  }

  @Data
  @Builder
  @AllArgsConstructor
  @Schema(name = "NaturePreviewDto", description = "7대 자연 프리뷰 정보 DTO")
  public static class PreviewDto {

    @Schema(description = "7대 자연 게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @QueryProjection
    public PreviewDto(Long id, String title, String originUrl, String thumbnailUrl,
        String addressTag) {
      this.id = id;
      this.title = title;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
      this.addressTag = addressTag;
    }
  }

  @Getter
  @Builder
  @Schema(name = "NatureDetailDto", description = "7대 자연 상세 정보")
  public static class DetailDto {

    @Schema(description = "7대 자연 게시물 id")
    private Long id;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "소개")
    private String intro;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "연락처")
    private String contact;

    @Schema(description = "이용 시간")
    private String time;

    @Schema(description = "입장료")
    private String fee;

    @Schema(description = "상세 정보")
    private String details;

    @Schema(description = "편의시설")
    private String amenity;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @Schema(description = "이미지 리스트")
    private List<ImageFileDto> images;
  }
}
