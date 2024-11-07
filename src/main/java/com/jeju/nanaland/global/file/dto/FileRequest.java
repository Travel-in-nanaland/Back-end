package com.jeju.nanaland.global.file.dto;

import com.jeju.nanaland.domain.common.annotation.EnumValid;
import com.jeju.nanaland.global.file.data.FileCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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

  @Schema(description = "파일 업로드 완료 요청 Dto")
  @Data
  @Builder
  public static class CompleteCommandDto {

    @NotBlank
    @Schema(description = "UPLOAD ID")
    private String uploadId;

    @NotBlank
    @Schema(description = "파일 키")
    private String fileKey;

    @NotEmpty
    @Schema(description = "파트 정보 리스트")
    private List<PartInfo> parts;
  }

  @Data
  @Builder
  public static class PartInfo {
    private int partNumber;
    private String eTag;
  }
}
