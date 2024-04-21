package com.jeju.nanaland.domain.nature.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class NatureResponse {

  @Getter
  @Builder
  public static class NatureThumbnailDto {

    private Long totalElements;
    private List<NatueThumbnail> data;

  }

  @Getter
  @Builder
  public static class NatueThumbnail {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private String addressTag;
  }

}
