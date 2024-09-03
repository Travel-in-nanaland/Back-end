package com.jeju.nanaland.domain.favorite.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class FavoriteResponse {

  @Data
  @Builder
  @Schema(name = "FavoriteThumbnailsDto", description = "찜리스트 조회 결과")
  public static class ThumbnailsDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(description = "찜 리스트 데이터 리스트")
    private List<Thumbnail> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class Thumbnail {

    @Schema(description = "게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "게시물 카테고리")
    private String category;

    @Schema(description = "썸네일 이미지")
    private ImageFileDto firstImage;

    @QueryProjection
    public Thumbnail(Long id, String title, String category, String originUrl,
        String thumbnailUrl) {
      this.id = id;
      this.title = title;
      this.category = category;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    }
  }

  @Data
  @Builder
  @Schema(description = "좋아요 상태 결과")
  public static class StatusDto {

    @Schema(description = "좋아요 상태")
    private boolean isFavorite;
  }
}
