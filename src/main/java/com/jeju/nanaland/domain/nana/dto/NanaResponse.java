package com.jeju.nanaland.domain.nana.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class NanaResponse {

  @Data
  @Builder
  public static class ThumbnailDto {

    private Long id;

    //이때 imageFile id까지 주는 것이 좋은가?
    //private Long imageFileId;
    @NotBlank
    private String thumbnailUrl;

  }

  @Data
  @Builder
  public static class nanaDetailDto {

    @NotBlank
    private String titleImageUrl;

    @NotBlank
    private String notice;

    private List<nanaDetail> nanaDetails;
  }

  @Data
  @Builder
  public static class nanaDetail {

    public int number;

    @NotBlank
    public String subTitle;

    @NotBlank
    public String title;

    @NotBlank
    public String imageUrl;

    @NotBlank
    public String content;

  }
}
