package com.jeju.nanaland.domain.report.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.report.entity.infoFix.FixType;
import com.jeju.nanaland.domain.report.entity.claim.ClaimType;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class ReportRequest {

  @Data
  @Schema(description = "정보 수정 요청 Dto")
  @AllArgsConstructor
  @Builder
  public static class InfoFixDto {

    @NotNull
    @Schema(description = "수정 요청 게시물 id")
    private Long postId;

    @EnumValid(
        enumClass = FixType.class,
        message = "정보 수정 요청 타입이 유효하지 않습니다."
    )
    @Schema(
        description = "정보 수정 요청 타입",
        example = "CONTACT_OR_HOMEPAGE",
        allowableValues = {"CONTACT_OR_HOMEPAGE", "LOCATION", "TIME", "PRICE", "DELETE_LOCATION",
            "ETC"}
    )
    private String fixType;

    @EnumValid(
        enumClass = Category.class,
        message = "게시물 카테고리 값이 유효하지 않습니다."
    )
    @Schema(
        description = "게시물 카테고리",
        example = "EXPERIENCE",
        allowableValues = {"NANA", "EXPERIENCE", "FESTIVAL", "NATURE", "PRICE", "MARKET"}
    )
    private String category;

    @NotBlank
    @Schema(description = "정보 수정 내용")
    private String content;

    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "이메일 형식이 올바르지 않습니다.")
    @Schema(
        description = "이메일 정규식: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\\$",
        example = "test@naver.com"
    )
    private String email;

    @Schema(description = "파일 키 리스트", example = "[\"test/fileKey1.jpg\", \"test/fileKey2.jpeg\", \"test/fileKey3.png\"]")
    private List<String> fileKeys;
  }

  @Data
  @AllArgsConstructor
  @Builder
  @Schema(description = "신고 요청 Dto")
  public static class ClaimReportDto {

    @NotNull
    @Schema(description = "회원 ID 또는 리뷰 ID")
    private Long id;

    @EnumValid(
        enumClass = ClaimReportType.class,
        message = "신고 타입이 유효하지 않습니다."
    )
    @Schema(
        description = "신고 타입",
        example = "REVIEW",
        allowableValues = {"MEMBER", "REVIEW"}
    )
    private String reportType;

    @EnumValid(
        enumClass = ClaimType.class,
        message = "신고 사유 타입이 유효하지 않습니다."
    )
    @Schema(
        description = "리뷰 신고 사유 타입",
        example = "COMMERCIAL_PURPOSE",
        allowableValues = {"COMMERCIAL_PURPOSE", "DISLIKE", "PROFANITY", "PERSONAL_INFORMATION",
            "OBSCENITY", "FACILITY_ISSUE", "DRUGS", "VIOLENCE", "ETC"}
    )
    private String claimType;

    @NotBlank
    @Schema(description = "신고 내용", example = "claimReport claimReport")
    @Size(min = 20, message = "신고 사유는 20자 이상으로 작성해주세요.")
    @Size(max = 500, message = "신고 사유는 500자 이하로 작성해주세요.")
    private String content;

    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "이메일 형식이 올바르지 않습니다.")
    @Schema(
        description = "이메일 정규식: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\\$",
        example = "test@naver.com"
    )
    private String email;

    @Schema(description = "파일 키 리스트", example = "[\"test/fileKey1.jpg\", \"test/fileKey2.jpeg\", \"test/fileKey3.png\"]")
    private List<String> fileKeys;
  }
}
