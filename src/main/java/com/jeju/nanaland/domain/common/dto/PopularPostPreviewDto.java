package com.jeju.nanaland.domain.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "인기 게시물 조회 DTO")
public class PopularPostPreviewDto {

  @Schema(description = "게시물 id")
  private Long id;

  @Schema(description = "제목")
  private String title;

  @Schema(description = "주소")
  private String address;

  @Schema(
      description = "게시물 카테고리",
      example = "NATURE",
      allowableValues = {
          "NATURE", "ACTIVITY", "CULTURE_AND_ARTS", "FESTIVAL", "MARKET", "RESTAURANT"
      })
  private String category;

  @Schema(description = "썸네일 이미지")
  private ImageFileDto firstImage;

  @Schema(description = "좋아요 여부")
  private boolean isFavorite;

  @Schema(description = "조회수")
  private int viewCount;

  @QueryProjection
  public PopularPostPreviewDto(Long id, String title, String address, String category,
      String originUrl, String thumbnailUrl, int viewCount) {
    this.id = id;
    this.title = title;
    this.address = address;
    this.category = category;
    this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    this.viewCount = viewCount;
  }
}
