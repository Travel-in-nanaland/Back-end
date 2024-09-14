package com.jeju.nanaland.domain.favorite.dto;

import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class FavoriteResponse {

  @Data
  @Builder
  @Schema(name = "FavoritePreviewPageDto", description = "찜리스트 조회 결과")
  public static class PreviewPageDto {

    @Schema(description = "총 찜 리스트 개수")
    private Long totalElements;

    @Schema(name = "FavoritePostCardDto", description = "찜 리스트 데이터 리스트")
    private List<PreviewDto> data;
  }

  @Schema(name = "FavoritePreviewDto", description = "찜 게시물 preview 정보")
  public static class PreviewDto extends PostPreviewDto {

    public PreviewDto(PostPreviewDto postPreviewDto) {
      super(postPreviewDto);
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
