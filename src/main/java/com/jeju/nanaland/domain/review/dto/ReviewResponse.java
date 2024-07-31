package com.jeju.nanaland.domain.review.dto;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
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
  public static class ReviewStatusDto {

    @Schema(description = "좋아요 상태")
    private boolean isReviewHeart;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @Schema(description = "마이페이지에서 리뷰 수정 클릭 했을 때 원래 작성했던 리뷰 내용")
  public static class MyReviewDetailDto {

    @Schema(description = "리뷰 게시물 id")
    private Long id;


    @Schema(description = "post 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "post 장소 이름")
    private String title;

    @Schema(description = "post 장소 주소")
    private String address;

    @Schema(description = "리뷰 별점 (1~5)")
    private int rating;

    @Schema(description = "리뷰 내용")
    private String content;

    @Schema(description = "리뷰 이미지 리스트")
    private List<MyReviewImageDto> images;

    @Schema(
        description = "게시물 카테고리",
        example = "[\"ANNIVERSARY\", \"CUTE\"]",
        allowableValues = {"ANNIVERSARY", "CUTE", "LUXURY", "SCENERY", "KIND", "CHILDREN", "FRIEND",
            "PARENTS", "ALONE", "HALF", "RELATIVE", "PET", "OUTLET", "LARGE", "BATHROOM"}
    )
    private List<ReviewTypeKeyword> reviewKeywords;

    @QueryProjection
    public MyReviewDetailDto(Long id, String firstImageOriginUrl, String firstImageThumbnailUrl,
        String title, String address, int rating, String content) {
      this.id = id;
      this.firstImage = new ImageFileDto(firstImageOriginUrl, firstImageThumbnailUrl);
      this.title = title;
      this.address = address;
      this.rating = rating;
      this.content = content;
    }

    @Getter
    @Builder
    @Schema(description = "회원이 작성한 리뷰 이미지 정보")
    public static class MyReviewImageDto {

      private Long id;
      private String originUrl;
      private String thumbnailUrl;

    }
  }

  @Getter
  @Builder
  @Schema(description = "회원이 작성한 리뷰 리스트 페이징 정보")
  public static class MemberReviewListDto {

    @Schema(description = "리뷰 총 개수")
    private Long totalElements;

    @Schema(description = "리뷰 결과 리스트")
    private List<MemberReviewDetailDto> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class MemberReviewDetailDto {

    @Schema(description = "리뷰 게시물 id")
    private Long id;
    @Schema(description = "장소 게시물 id")
    private Long postId;
    @Schema(description = "장소 카테고리(이색체험, 맛집)")
    private Category category;
    @Schema(description = "장소명")
    private String placeName;
    @Schema(description = "리뷰 점수")
    private Double rating;
    @Schema(description = "리뷰 내용")
    private String content;
    @Schema(description = "리뷰 작성일")
    private LocalDate createdAt;
    @Schema(description = "리뷰 좋아요 개수")
    private Integer heartCount;
    @Schema(description = "리뷰 이미지 리스트")
    private List<ImageFileDto> images;
    @Schema(description = "리뷰 키워드 리스트")
    private Set<String> reviewTypeKeywords;

    @QueryProjection
    public MemberReviewDetailDto(Long id, Long postId, Category category,
        Integer rating, String content,
        LocalDateTime createdAt, Long heartCount) {
      this.id = id;
      this.postId = postId;
      this.category = category;
      this.rating = Math.round(rating * 100.0) / 100.0;
      this.content = content;
      this.createdAt = createdAt.toLocalDate();
      this.heartCount = Math.toIntExact(heartCount);
    }
  }

  @Getter
  @Builder
  @Schema(description = "회원이 작성한 리뷰 리스트 미리보기 정보")
  public static class MemberReviewPreviewDto {

    @Schema(description = "리뷰 총 개수")
    private Long totalElements;

    @Schema(description = "리뷰 결과 리스트")
    private List<MemberReviewPreviewDetailDto> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class MemberReviewPreviewDetailDto {

    @Schema(description = "리뷰 게시물 id")
    private Long id;
    @Schema(description = "장소 게시물 id")
    private Long postId;
    @Schema(description = "장소 카테고리(이색체험, 맛집)")
    private Category category;
    @Schema(description = "장소명")
    private String placeName;
    @Schema(description = "리뷰 작성일")
    private LocalDate createdAt;
    @Schema(description = "리뷰 좋아요 개수")
    private Integer heartCount;
    @Schema(description = "리뷰 이미지")
    private ImageFileDto imageFileDto;

    @QueryProjection
    public MemberReviewPreviewDetailDto(Long id, Long postId, Category category,
        LocalDateTime createdAt, Long heartCount) {
      this.id = id;
      this.postId = postId;
      this.category = category;
      this.createdAt = createdAt.toLocalDate();
      this.heartCount = Math.toIntExact(heartCount);
    }
  }
}