package com.jeju.nanaland.domain.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class PostPreviewDto {

  @Schema(description = "게시물 id")
  private Long id;

  @Schema(description = "제목")
  private String title;

  @Schema(description = "게시물 카테고리")
  private String category;

  @Schema(description = "썸네일 이미지")
  private ImageFileDto firstImage;

  @QueryProjection
  public PostPreviewDto(Long id, String title, String originUrl, String thumbnailUrl) {
    this.id = id;
    this.title = title;
    this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
  }

  public PostPreviewDto(PostPreviewDto postPreviewDto) {
    this.id = postPreviewDto.getId();
    this.title = postPreviewDto.getTitle();
    this.category = postPreviewDto.getCategory();
    this.firstImage = postPreviewDto.getFirstImage();
  }


}
