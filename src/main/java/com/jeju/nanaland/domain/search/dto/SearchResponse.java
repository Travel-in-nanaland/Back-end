package com.jeju.nanaland.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class SearchResponse {

  @Data
  @Builder
  @Schema
  public static class CategoryDto {

    @Schema(description = "축제 조회 결과")
    private ResultDto festival;

    @Schema(description = "7대자연 조회 결과")
    private ResultDto nature;

    @Schema(description = "이색체험 조회 결과")
    private ResultDto experience;

    @Schema(description = "전통시장 조회 결과")
    private ResultDto market;

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

    @Schema(description = "게시물 썸네일 이미지")
    private String thumbnailUrl;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;
  }
}
