package com.jeju.nanaland.domain.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class FavoriteResponse {

  @Data
  @Builder
  @Schema(name = "FavoriteCardPageDto", description = "찜리스트 조회 결과")
  public static class FavoriteCardPageDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(name = "FavoritePostCardDto", description = "찜 리스트 데이터 리스트")
    private List<FavoritePostCardDto> data;
  }

  @Data
  @Builder
  @Schema(description = "좋아요 상태 결과")
  public static class StatusDto {

    @Schema(description = "좋아요 상태")
    private boolean isFavorite;
  }
}
