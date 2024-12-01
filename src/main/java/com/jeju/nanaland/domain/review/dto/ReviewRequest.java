package com.jeju.nanaland.domain.review.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.review.entity.ReviewTypeKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class ReviewRequest {

  @Getter
  @Builder
  @Schema(description = "리뷰 생성")
  public static class CreateReviewDto {

    @Schema(description = "리뷰 별점 (1~5)")
    @Min(value = 1, message = "별점은 1~5점 사이 값 입니다.")
    @Max(value = 5, message = "별점은 1~5점 사이 값 입니다.")
    private int rating;

    @Schema(description = "리뷰 내용")
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(max = 200, message = "리뷰는 200글자 이하로 작성해야 합니다.")
    private String content;

    @EnumValid(
        enumClass = ReviewTypeKeyword.class,
        message = "해당 카테고리가 존재하지 않습니다."
    )
    @Schema(
        description = "게시물 카테고리",
        example = "[\"ANNIVERSARY\", \"CUTE\", \"PET\" ]",
        allowableValues = {"ANNIVERSARY", "CUTE", "LUXURY", "SCENERY", "KIND", "CHILDREN", "FRIEND",
            "PARENTS", "ALONE", "HALF", "RELATIVE", "PET", "OUTLET", "LARGE", "BATHROOM", "NONE"}
    )
    @Size(min = 1, message = "최소 1개 선택 가능합니다.")
    @Size(max = 6, message = "최대 6개까지 선택 가능합니다")
    private List<String> reviewKeywords;

    @Schema(description = "파일 키 리스트", example = "[\"test/fileKey1.jpg\", \"test/fileKey2.jpeg\", \"test/fileKey3.png\"]")
    private List<String> fileKeys;
  }

  @Getter
  @Builder
  @Schema(description = "리뷰 수정")
  public static class EditReviewDto {

    @Schema(description = "리뷰 별점 (1~5)")
    @Min(value = 1, message = "별점은 1~5점 사이 값 입니다.")
    @Max(value = 5, message = "별점은 1~5점 사이 값 입니다.")
    private int rating;

    @Schema(description = "리뷰 내용")
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(max = 200, message = "리뷰는 200글자 이하로 작성해야 합니다.")
    private String content;

    @EnumValid(
        enumClass = ReviewTypeKeyword.class,
        message = "해당 카테고리가 존재하지 않습니다."
    )
    @Schema(
        description = "게시물 카테고리",
        example = "[\"ANNIVERSARY\", \"CUTE\", \"PET\" ]",
        allowableValues = {"ANNIVERSARY", "CUTE", "LUXURY", "SCENERY", "KIND", "CHILDREN", "FRIEND",
            "PARENTS", "ALONE", "HALF", "RELATIVE", "PET", "OUTLET", "LARGE", "BATHROOM"}
    )
    @Size(min = 1, message = "최소 1개 선택 가능합니다.")
    @Size(max = 6, message = "최대 6개까지 선택 가능합니다")
    private List<String> reviewKeywords;

    @Schema(description = "리뷰 이미지 수정 정보 리스트 => fileKeys와 newImage = true인 것과 수 같아야 함 /"
        + " 수정되어 제출되는 리뷰에 이미지가 없다면 null이 아닌 빈 리스트 [] 를 보내야 합니다.")
    @NotNull
    private List<EditImageInfoDto> editImageInfoList;

    @Schema(description = "파일 키 리스트", example = "[\"test/fileKey1.jpg\", \"test/fileKey2.jpeg\", \"test/fileKey3.png\"]")
    private List<String> fileKeys;

    @Getter
    @Builder
    @Schema(description = "리뷰 이미지 수정 정보 ")
    public static class EditImageInfoDto {

      @Schema(description = "리뷰 이미지 id / 존재하던 사진이면 원래 id, 새로 추가된 사진이면 -1")
      private Long id;

      @Schema(description = "이미지 수정 여부 / 존재하던 사진이면 false, 새로 추가된 사진이면 true")
      private boolean newImage;
    }

  }
}
