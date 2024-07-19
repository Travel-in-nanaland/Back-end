package com.jeju.nanaland.domain.review.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewResponse {

  @Getter
  @Builder
  @Schema(description = "리뷰 리스트 페이징 정보")
  public static class ReviewListDto {

    @Schema(description = "리뷰 총 개수")
    private Long totalElements;

    @Schema(description = "리뷰 총 평균 점수")
    private Double totalAvgRating;

    @Schema(description = "리뷰 결과 리스트")
    private List<ReviewDetailDto> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ReviewDetailDto {

    @Schema(description = "리뷰 게시물 id")
    private Long id;
    @Schema(description = "작성자 id")
    private Long memberId;
    @Schema(description = "작성자 닉네임")
    private String nickname;
    @Schema(description = "작성자 프로필 사진")
    private ImageFileDto profileImage;
    @Schema(description = "작성자 총 리뷰 개수")
    private Integer memberReviewCount;
    @Schema(description = "리뷰 점수")
    private Double rating;
    @Schema(description = "리뷰 내용")
    private String content;
    @Schema(description = "리뷰 작성일")
    private LocalDate createdAt;
    @Schema(description = "리뷰 좋아요 개수")
    private Integer heartCount;
    @Schema(description = "현 로그인 회원의 좋아요 여부")
    private boolean isReviewHeart;
    @Schema(description = "리뷰 이미지 리스트")
    private List<ImageFileDto> images;
    @Schema(description = "리뷰 키워드 리스트")
    private Set<String> reviewTypeKeywords;

    @QueryProjection
    public ReviewDetailDto(Long id, Long memberId, String nickname, ImageFileDto imageFileDto,
        Long memberReviewCount, Integer rating, String content,
        LocalDateTime createdAt, Long heartCount, boolean isReviewHeart) {
      this.id = id;
      this.memberId = memberId;
      this.nickname = nickname;
      this.profileImage = imageFileDto;
      this.memberReviewCount = Math.toIntExact(memberReviewCount);
      this.rating = Math.round(rating * 100.0) / 100.0;
      this.content = content;
      this.createdAt = createdAt.toLocalDate();
      this.heartCount = Math.toIntExact(heartCount);
      this.isReviewHeart = isReviewHeart;
    }
  }

  @Data
  @Builder
  @Schema(description = "좋아요 상태 결과")
  public static class StatusDto {

    @Schema(description = "좋아요 상태")
    private boolean isReviewHeart;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "리뷰 작성위한 게시물 검색")
  public static class SearchPostForReviewDto {

    @Schema(description = "리뷰 게시물 id")
    private Long id;

    @Schema(description = "게시물 카테고리")
    private String category;

    @Schema(description = "게시물 제목")
    private String title;

    @Schema(description = "게시물 썸네일")
    private ImageFileDto firstImage;

    @Schema(description = "게시물 주소")
    private String address;

    @QueryProjection
    public SearchPostForReviewDto(Long id, String category, String title, ImageFile firstImage,
        String address) {
      this.id = id;
      this.category = category;
      this.title = title;
      this.firstImage = new ImageFileDto(firstImage.getOriginUrl(), firstImage.getThumbnailUrl());
      this.address = address;
    }
  }

}
