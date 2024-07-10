package com.jeju.nanaland.domain.review.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class ReviewRequest {

  @Getter
  @Builder
  @Schema(description = "리뷰 리스트 페이징 정보")
  public static class CreateReviewDto {

    @Schema(description = "리뷰 별점 (1~5)")
    @Min(value = 1, message = "별점은 1~5점 사이 값 입니다.")
    @Max(value = 5, message = "별점은 1~5점 사이 값 입니다.")
    private int rating;

    //TODO 최소 글자 수, 최대 공백 포함인지 확인하기
    @Schema(description = "리뷰 내용")
    private String content;

    @EnumValid(
        enumClass = ReviewTypeKeyword.class,
        message = "해당 카테고리가 존재하지 않습니다."
    )
    @Schema(
        description = "게시물 카테고리",
        example = "[ANNIVERSARY, CUTE]",
        allowableValues = {"ANNIVERSARY", "CUTE", "LUXURY", "SCENERY", "KIND", "CHILDREN", "FRIEND",
            "PARENTS", "ALONE", "HALF", "RELATIVE", "PET", "OUTLET", "LARGE", "BATHROOM"}
    )
    @Size(min = 3, message = "최소 3개 선택 가능합니다.")
    @Size(max = 6, message = "최대 6개까지 선택 가능합니다")
    private List<String> reviewKeywords;

  }

}
