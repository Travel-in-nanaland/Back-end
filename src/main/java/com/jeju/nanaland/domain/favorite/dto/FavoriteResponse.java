package com.jeju.nanaland.domain.favorite.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class FavoriteResponse {

  @Data
  @Builder
  @Schema(description = "전체 찜리스트 조회 결과")
  public static class AllCategoryDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  @Schema(description = "7개자연 찜리스트 조회 결과")
  public static class NatureDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  @Schema(description = "축제 찜리스트 조회 결과")
  public static class FestivalDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  @Schema(description = "나나스픽 찜리스트 조회 결과")
  public static class NanaDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  @Schema(description = "이색체험 찜리스트 조회 결과")
  public static class ExperienceDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  @Schema(description = "전통시장 찜리스트 조회 결과")
  public static class MarketDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  @Schema(description = "좋아요 상태 결과")
  public static class StatusDto {

    @Schema(description = "좋아요 상태")
    private boolean isFavorite;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ThumbnailDto {

    @Schema(description = "게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "썸네일 url")
    private String thumbnailUrl;

    @Schema(description = "게시물 카테고리")
    private String category;

    @QueryProjection
    public ThumbnailDto(Long id, String title, String thumbnailUrl) {
      this.id = id;
      this.title = title;
      this.thumbnailUrl = thumbnailUrl;
    }
  }
}
