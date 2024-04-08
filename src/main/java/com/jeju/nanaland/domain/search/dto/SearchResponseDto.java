package com.jeju.nanaland.domain.search.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

public class SearchResponseDto {

  @Data
  @Builder
  public static class Category {

    private Result stay;
    private Result festival;
    private Result nature;
    private Result experience;
    private Result market;
  }

  @Data
  public static class Story {

  }

  @Data
  @Builder
  public static class Result {

    private Long count;
    private List<Thumbnail> data;
  }

  @Data
  @Builder
  public static class Thumbnail {

    private Long id;
    private String thumbnailUrl;
    private String title;
  }
}
