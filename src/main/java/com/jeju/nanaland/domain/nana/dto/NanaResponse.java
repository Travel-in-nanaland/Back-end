package com.jeju.nanaland.domain.nana.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class NanaResponse {

  @Data
  @Builder
  public static class ThumbnailDto {

    private Long count;

    private List<NanaThumbnail> data;


  }

  @Data
  @Builder
  public static class NanaThumbnail {

    private Long id;

    @NotBlank
    private String thumbnailUrl;


    @QueryProjection
    public NanaThumbnail(Long id, String thumbnailUrl) {
      this.id = id;
      this.thumbnailUrl = thumbnailUrl;
    }
  }

  @Data
  @Builder
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

    @QueryProjection
    public nanaDetail(int number, String subTitle, String title, String imageUrl, String content) {
      this.number = number;
      this.subTitle = subTitle;
      this.title = title;
      this.imageUrl = imageUrl;
      this.content = content;
    }
  }
}
