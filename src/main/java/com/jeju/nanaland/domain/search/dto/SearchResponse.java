package com.jeju.nanaland.domain.search.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class SearchResponse {

  @Data
  @Builder
  @Schema
  public static class AllCategoryDto {

    @Schema(description = "축제 조회 결과")
    private ResultDto festival;

    @Schema(description = "7대자연 조회 결과")
    private ResultDto nature;

    @Schema(description = "액티비티 조회 결과")
    private ResultDto activity;

    @Schema(description = "문화예술 조회 결과")
    private ResultDto cultureAndArts;

    @Schema(description = "전통시장 조회 결과")
    private ResultDto market;

    @Schema(description = "제주 맛집 조회 결과")
    private ResultDto restaurant;

    @Schema(description = "나나스픽 조회 결과")
    private ResultDto nana;
  }

  @Data
  @Builder
  public static class ResultDto {

    @Schema(description = "총 항목 개수")
    private Long totalElements;

    @Schema(description = "결과 데이터")
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  public static class ThumbnailDto {

    @Schema(description = "게시물 id")
    private Long id;

    @Schema(description = "게시물 카테고리")
    private String category;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;
  }

  @Data
  @Builder
  public static class SearchVolumeDto {

    @Schema(description = "게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "게시물 카테고리")
    private String category;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;
  }
}
