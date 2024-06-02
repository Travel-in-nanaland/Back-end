package com.jeju.nanaland.domain.report.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.report.entity.FixType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class ReportRequest {

  @Data
  @Schema(description = "정보 수정 요청 Dto")
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
        enumClass = CategoryContent.class,
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
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\\$",
        message = "이메일 형식이 올바르지 않습니다.")
    @Schema(
        description = "이메일 정규식: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}\\$",
        example = "test@naver.com"
    )
    private String email;
  }
}
