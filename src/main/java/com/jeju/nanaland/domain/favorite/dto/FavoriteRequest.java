package com.jeju.nanaland.domain.favorite.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.common.data.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class FavoriteRequest {

  @Schema(description = "좋아요 토글 요청 Dto")
  @Data
  public static class LikeToggleDto {

    @NotNull
    private Long id;

    @EnumValid(
        enumClass = Category.class,
        message = "해당 카테고리가 존재하지 않습니다."
    )
    @Schema(
        description = "게시물 카테고리",
        example = "NATURE",
        allowableValues = {"NANA", "EXPERIENCE", "FESTIVAL", "NATURE", "MARKET"}
    )
    private String category;
  }
}
