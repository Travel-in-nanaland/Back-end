package com.jeju.nanaland.domain.experience.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

public class ExperienceResponse {

  @Data
  @Builder
  @Schema(description = "이색체험 게시물 페이징 정보")
  public static class ExperienceThumbnailDto {

    @Schema(description = "이색체험 전체 게시물 수")
    private Long totalElements;

    @Schema(description = "이색체험 게시물 결과 리스트")
    private List<ExperienceThumbnail> data;
  }

  @Data
  @Setter
  @Builder
  @AllArgsConstructor
  @Schema(description = "이색체험 게시물 정보")
  public static class ExperienceThumbnail {

    @Schema(description = "이색체험 게시물 id")
    private Long id;

    @NotBlank
    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "이색체험 게시물 제목")
    private String title;

    @NotBlank
    @Schema(description = "위치 정보 태그")
    private String addressTag;

    @Schema(description = "리뷰 평점 평균")
    private Double ratingAvg;

    @NotBlank
    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @QueryProjection
    public ExperienceThumbnail(Long id, String originUrl, String thumbnailUrl, String title,
        String addressTag) {
      this.id = id;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
      this.title = title;
      this.addressTag = addressTag;
    }
  }

  @Data
  @Builder
  @Schema(description = "이색체험 상세 정보")
  public static class ExperienceDetailDto {

    @Schema(description = "이색체험 게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "간단 설명")
    private String intro;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "위치")
    private String address;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "연락처")
    private String contact;

    @Schema(description = "홈페이지")
    private String homepage;

    @Schema(description = "이용시간")
    private String time;

    @Schema(description = "편의시설")
    private String amenity;

    @Schema(description = "상세 정보")
    private String details;

    @Schema(description = "키워드")
    private List<String> keywords;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @Schema(description = "이미지 리스트")
    private List<ImageFileDto> images;
  }
}
