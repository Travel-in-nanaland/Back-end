package com.jeju.nanaland.domain.search.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

public class SearchResponse {

  @Data
  @Builder
  public static class CategoryDto {

    private ResultDto stay;
    private ResultDto festival;
    private ResultDto nature;
    private ResultDto experience;
    private ResultDto market;
  }

  @Data
  public static class StoryDto {

  }

  @Data
  @Builder
  public static class ResultDto {

    private Long count;
    private List<ThumbnailDto> data;
  }

  @Data
  @Builder
  public static class ThumbnailDto {

    private Long id;
    private String thumbnailUrl;
    private String title;
  }
}
