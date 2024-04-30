package com.jeju.nanaland.domain.festival.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class FestivalResponse {

  @Data
  @Builder
  @Schema(description = "축제 썸네일 조회 DTO")
  public static class FestivalThumbnailDto {

    @Schema(description = "총 조회 개수")
    private Long totalElements;

    @Schema(description = "결과 데이터")
    private List<FestivalThumbnail> data;


  }

  @Data
  @Builder
  @Schema(description = "축제 썸네일 조회 DTO")
  public static class FestivalThumbnail {

    @Schema(description = "축제 게시물 id")
    private Long id;

    @Schema(description = "축제 이름")
    private String title;

    @NotBlank
    @Schema(description = "축제 썸네일 url")
    private String thumbnailUrl;

    @Schema(description = "주소 태그")
    private String addressTag;

    @NotBlank
    private String period;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

  }

}
