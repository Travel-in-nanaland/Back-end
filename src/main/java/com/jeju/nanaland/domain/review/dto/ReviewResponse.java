package com.jeju.nanaland.domain.review.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
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
  public static class ReviewListDto {

    private Long totalElements;
    private Double totalAvgRating;

    private List<ReviewDetailDto> data;
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class ReviewDetailDto {

    private Long id;
    private String nickname;
    private ImageFileDto profileImage;
    private Integer memberReviewCount;
    private Double memberReviewAvgRating;
    private String content;
    private LocalDate createdAt;
    private Integer heartCount;
    private boolean isFavorite;
    private List<ImageFileDto> images;
    private Set<String> reviewTypeKeywords;

    @QueryProjection
    public ReviewDetailDto(Long id, String nickname, ImageFileDto imageFileDto,
        Long memberReviewCount, Double memberReviewRatingAvg, String content,
        LocalDateTime createdAt, Long heartCount, boolean isFavorite) {
      this.id = id;
      this.nickname = nickname;
      this.profileImage = imageFileDto;
      this.memberReviewCount = Math.toIntExact(memberReviewCount);
      this.memberReviewAvgRating = Math.round(memberReviewRatingAvg * 100.0) / 100.0;
      this.content = content;
      this.createdAt = createdAt.toLocalDate();
      this.heartCount = Math.toIntExact(heartCount);
      this.isFavorite = isFavorite;
    }
  }
}
