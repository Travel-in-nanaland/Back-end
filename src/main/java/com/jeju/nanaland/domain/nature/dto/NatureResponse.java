package com.jeju.nanaland.domain.nature.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class NatureResponse {

  @Getter
  @Builder
  @Schema(description = "7대 자연 게시물 페이징 정보")
  public static class NatureThumbnailDto {

    @Schema(description = "7대 자연 전체 게시물 수")
    private Long totalElements;

    @Schema(description = "7대 자연 게시물 결과 리스트")
    private List<NatureThumbnail> data;

  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class NatureThumbnail {

    @Schema(description = "7대 자연 게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto imageFileDto;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @QueryProjection
    public NatureThumbnail(Long id, String title, String originUrl, String thumbnailUrl,
        String addressTag) {
      this.id = id;
      this.title = title;
      this.imageFileDto = new ImageFileDto(originUrl, thumbnailUrl);
      this.addressTag = addressTag;
    }
  }

  @Getter
  @Builder
  @Schema(description = "7대 자연 상세 정보")
  public static class NatureDetailDto {

    @Schema(description = "7대 자연 게시물 id")
    private Long id;

    @Schema(description = "원본 이미지 url")
    private String originUrl;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "소개")
    private String intro;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "연락처")
    private String contact;

    @Schema(description = "이용 시간")
    private String time;

    @Schema(description = "입장료")
    private String fee;

    @Schema(description = "상세 정보")
    private String details;

    @Schema(description = "편의시설")
    private String amenity;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;
  }
}
