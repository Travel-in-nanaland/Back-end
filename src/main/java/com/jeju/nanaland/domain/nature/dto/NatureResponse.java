package com.jeju.nanaland.domain.nature.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class NatureResponse {

  @Getter
  @Builder
  public static class NatureThumbnailDto {

    private Long totalElements;
    private List<NatureThumbnail> data;

  }

  @Getter
  @Builder
  public static class NatureThumbnail {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private String address;
    private boolean isFavorite;

    @QueryProjection
    public NatureThumbnail(Long id, String title, String thumbnailUrl, String address,
        boolean isFavorite) {
      this.id = id;
      this.title = title;
      this.thumbnailUrl = thumbnailUrl;
      this.address = address;
      this.isFavorite = isFavorite;
    }
  }

  @Getter
  @Builder
  public static class NatureDetailDto {

    private Long id;
    private String originUrl;
    private String addressTag;
    private String title;
    private String content;
    private String intro;
    private String address;
    private String contact;
    private String time;
    private String fee;
    private String details;
    private String amenity;
    private boolean isFavorite;
  }
}
