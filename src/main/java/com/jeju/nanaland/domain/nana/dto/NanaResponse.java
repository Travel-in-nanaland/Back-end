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
  public static class NanaThumbnailDto {

    @Schema(description = "총 조회 개수")
    private Long totalElements;

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
  public static class NanaDetailDto {

    @NotBlank
    @Schema(description = "게시물 url")
    private String originUrl;

    @NotBlank
    @Schema(description = "알아두면 좋아요! 내용")
    private String notice;

    @Schema(description = "게시물 데이터")
    private List<NanaDetail> nanaDetails;

  }

  @Data
  @Builder
  @Schema(description = "나나's pick 게시글 세부 내용")
  public static class NanaDetail {

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

    public List<NanaAdditionalInfo> additionalInfoList;

  }

  @Data
  @Builder
  @Schema(description = "나나's pick 각 게시글 부가 정보")
  public static class NanaAdditionalInfo {

    @Schema(description = "부가 정보 key 값 ex: 주차정보, 스페셜, 예약링크,,")
    public String infoKey;

    @Schema(description = "부가 정보 value 값")
    public String infoValue;

  }
}
