package com.jeju.nanaland.domain.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

public class FavoriteResponse {

  @Data
  @Builder
  public static class StatusDto {

    @Schema(description = "좋아요 상태")
    private boolean isFavorite;
  }
}
