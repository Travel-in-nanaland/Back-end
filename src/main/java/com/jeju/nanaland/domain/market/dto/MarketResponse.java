package com.jeju.nanaland.domain.market.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class MarketResponse {

  @Data
  @Builder
  @Schema(description = "전통시장 게시물 페이징 정보")
  public static class MarketThumbnailDto {

    @Schema(description = "전통시장 전체 게시물 수")
    private Long totalElements;

    @Schema(description = "전통시장 게시물 결과 리스트")
    private List<MarketThumbnail> data;
  }

  @Data
  @Builder
  @Schema(description = "전통시장 상세 정보")
  public static class MarketDetailDto {

    @Schema(description = "전통시장 게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "원본 이미지 url")
    private String originUrl;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "위치")
    private String address;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "연락처")
    private String contact;

    @Schema(description = "홈페이지")
    private String homepage;

    @Schema(description = "이용시간")
    private String time;

    @Schema(description = "편의시설")
    private String amenity;
  }

  @Data
  @Builder
  @Schema(description = "전통시장 게시물 정보")
  public static class MarketThumbnail {

    @Schema(description = "전통시장 게시물 id")
    private Long id;

    @Schema(description = "전통시장 게시물 제목")
    private String title;

    @NotBlank
    @Schema(description = "전통시장 게시물 썸네일 url")
    private String thumbnailUrl;

    @NotBlank
    @Schema(description = "위치 정보 태그")
    private String addressTag;

    @QueryProjection
    public MarketThumbnail(Long id, String title, String thumbnailUrl, String address) {
      this.id = id;
      this.title = title;
      this.thumbnailUrl = thumbnailUrl;
      this.addressTag = address;
    }
  }
}
