package com.jeju.nanaland.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class FileResponse {

  @Data
  @Builder
  @Schema(description = "파트별 Pre-Signed URL 정보")
  public static class InitResultDto {
    @Schema(description = "UPLOAD ID")
    private String uploadId;
    @Schema(description = "파일 키")
    private String fileKey;
    @Schema(description = "Pre-Signed URL 정보 리스트")
    List<PresignedUrlInfo> presignedUrlInfos;
  }

  @Data
  @Builder
  @Schema(description = "Pre-Signed URL 정보 리스트")
  public static class PresignedUrlInfo {
    private int partNumber;
    private String preSignedUrl;
  }
}
