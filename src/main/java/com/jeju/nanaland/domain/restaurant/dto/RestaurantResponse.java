package com.jeju.nanaland.domain.restaurant.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class RestaurantResponse {

  @Getter
  @Builder
  @Schema(description = "맛집 게시물 페이징 정보")
  public static class RestaurantThumbnailDto {

    @Schema(description = "맛집 전체 게시물 수")
    private Long totalElements;

    @Schema(description = "맛집 게시물 결과 리스트")
    private List<RestaurantResponse.RestaurantThumbnail> data;

  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class RestaurantThumbnail {

    @Schema(description = "맛집 게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "리뷰 평점 평균")
    private Double ratingAvg;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @QueryProjection
    public RestaurantThumbnail(Long id, String title, String originUrl, String thumbnailUrl,
        String addressTag) {
      this.id = id;
      this.title = title;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
      this.addressTag = addressTag;
    }
  }

  @Data
  @Builder
  @Schema(description = "제주맛집 상세 정보")
  public static class RestaurantDetailDto {

    @Schema(description = "제주맛집 게시물 id")
    private Long id;

    @Schema(description = "제목")
    private String title;

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

    @Schema(description = "인스타그램")
    private String instagram;

    @Schema(description = "이용시간")
    private String time;

    @Schema(description = "제공 서비스")
    private String service;

    @Schema(description = "메뉴")
    private List<RestaurantMenuDto> menus;

    @Schema(description = "키워드")
    private List<String> keywords;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @Schema(description = "이미지 리스트")
    private List<ImageFileDto> images;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @Schema(description = "메뉴 리스트")
  public static class RestaurantMenuDto {

    @Schema(description = "메뉴 이름")
    private String menuName;

    @Schema(description = "가격")
    private String price;

    @Schema(description = "이미지")
    private ImageFileDto firstImage;

    @QueryProjection
    public RestaurantMenuDto(String menuName, String price, String originUrl, String thumbnailUrl) {
      this.menuName = menuName;
      this.price = price;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    }
  }
}
