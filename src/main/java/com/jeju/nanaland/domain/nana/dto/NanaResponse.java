package com.jeju.nanaland.domain.nana.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class NanaResponse {

  @Data
  @Builder
  @Schema(description = "나나's pick 전체 썸네일 조회 DTO")
  public static class ThumbnailDto {

    @Schema(description = "총 조회 개수")
    private Long count;

    @Schema(description = "결과 데이터")
    private List<NanaThumbnail> data;


  }

  @Data
  @Builder
  @Schema(description = "나나's pick 개별 썸네일 조회 DTO")
  public static class NanaThumbnail {

    @Schema(description = "게시물 id")
    private Long id;

    @NotBlank
    @Schema(description = "게시물 썸네일 url")
    private String thumbnailUrl;


    @QueryProjection
    public NanaThumbnail(Long id, String thumbnailUrl) {
      this.id = id;
      this.thumbnailUrl = thumbnailUrl;
    }
  }

  @Data
  @Builder
  @Schema(description = "나나's pick 개별 상세 조회 DTO")
  public static class nanaDetailDto {

    @NotBlank
    @Schema(description = "게시물 url")
    private String originUrl;

    @NotBlank
    @Schema(description = "알아두면 좋아요! 내용")
    private String notice;

    @Schema(description = "게시물 데이터")
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
  @Schema(description = "나나's pick 게시글 세부 내용")
  public static class nanaDetail {

    @Schema(description = "순위")
    public int number;

    @NotBlank
    @Schema(description = "부제목")
    public String subTitle;

    @NotBlank
    @Schema(description = "제목")
    public String title;

    @NotBlank
    @Schema(description = "이미지 원본 url")
    public String imageUrl;

    @NotBlank
    @Schema(description = "게시물 설명")
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
