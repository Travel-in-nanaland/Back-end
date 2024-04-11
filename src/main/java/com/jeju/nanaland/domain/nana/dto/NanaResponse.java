package com.jeju.nanaland.domain.nana.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class NanaResponse {

  @Data
  public static class ThumbnailDto {

    private Long id;

    //이때 imageFile id까지 주는 것이 좋은가?
    //private Long imageFileId;
    @NotBlank
    private String thumbnailUrl;

    @QueryProjection
    public ThumbnailDto(Long id, String thumbnailUrl) {
      this.id = id;
      this.thumbnailUrl = thumbnailUrl;
    }
  }

  @Data
  public static class nanaDetailDto {

    @NotBlank
    private String originUrl;

    @NotBlank
    private String notice;

    private List<nanaDetail> nanaDetails;

    @QueryProjection
    public nanaDetailDto(String originUrl, String notice, List<nanaDetail> nanaDetails) {
      this.originUrl = originUrl;
      this.notice = notice;
      this.nanaDetails = nanaDetails;
    }
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
