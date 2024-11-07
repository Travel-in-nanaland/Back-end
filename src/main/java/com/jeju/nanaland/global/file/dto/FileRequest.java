package com.jeju.nanaland.global.file.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.global.file.data.FileCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class FileRequest {

  @Schema(description = "파일 업로드 시작 요청 Dto")
  @Data
  @Builder
  public static class InitCommandDto {

    @NotBlank
    @Schema(description = "원본 파일명 (확장자 포함)")
    private String originalFileName;

    @NotNull
    @Schema(description = "파일 크기")
    private Long fileSize;

    @EnumValid(
        enumClass = FileCategory.class,
        message = "해당 파일 카테고리가 존재하지 않습니다."
    )
    @Schema(
        description = "파일 카테고리",
        example = "MEMBER_PROFILE",
        allowableValues = {"MEMBER_PROFILE", "REVIEW", "INFO_FIX_REPORT", "CLAIM_REPORT"}
    )
    private String fileCategory;
  }
}
