package com.jeju.nanaland.global.file.service;

import static com.jeju.nanaland.global.exception.ErrorCode.FILE_UPLOAD_FAIL;
import static com.jeju.nanaland.global.exception.ErrorCode.INVALID_FILE_EXTENSION_TYPE;
import static com.jeju.nanaland.global.exception.ErrorCode.INVALID_FILE_SIZE;
import static com.jeju.nanaland.global.exception.ErrorCode.NO_FILE_EXTENSION;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.file.data.FileCategory;
import com.jeju.nanaland.global.file.dto.FileRequest;
import com.jeju.nanaland.global.file.dto.FileResponse;
import com.jeju.nanaland.global.file.dto.FileResponse.InitResultDto;
import com.jeju.nanaland.global.file.dto.FileResponse.PresignedUrlInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.s3.memberProfileDirectory}")
  private String MEMBER_PROFILE_DIRECTORY;
  @Value("${cloud.aws.s3.reviewDirectory}")
  private String REVIEW_DIRECTORY;
  @Value("${cloud.aws.s3.infoFixReportImageDirectory}")
  private String INFO_FIX_REPORT_DIRECTORY;
  @Value("${cloud.aws.s3.claimReportFileDirectory}")
  private String CLAIM_REPORT_DIRECTORY;
  private static final int PART_SIZE = 5 * 1024 * 1024;

  public FileResponse.InitResultDto uploadInit(FileRequest.InitCommandDto initCommandDto) {
    // 파일 크기 유효성 검사
    validateFileSize(initCommandDto.getFileSize());

    // 파일 형식 유효성 검사
    String contentType = validateFileExtension(initCommandDto.getOriginalFileName(),
        initCommandDto.getFileCategory());

    // S3 key 생성
    String fileKey = generateUniqueFileKey(initCommandDto.getOriginalFileName(),
        initCommandDto.getFileCategory());

    // ObjectMetadata 설정
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    metadata.setContentLength(initCommandDto.getFileSize());

    // Upload ID 발급
    try {
      InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket,
          fileKey)
          .withObjectMetadata(metadata);
      InitiateMultipartUploadResult initResponse = amazonS3.initiateMultipartUpload(initRequest);
      String uploadId = initResponse.getUploadId();

    int partCount = calculatePartCount(initCommandDto.getFileSize());
    List<PresignedUrlInfo> presignedUrlInfos = new ArrayList<>();

    // 파트별 Pre-Signed URL 발급
    for (int partNumber = 1; partNumber <= partCount; partNumber++) {
      GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileKey)
          .withMethod(HttpMethod.PUT)
          .withExpiration(getPresignedUrlExpiration())
          .withKey(fileKey);

      presignedUrlRequest.addRequestParameter("partNumber", String.valueOf(partNumber));
      presignedUrlRequest.addRequestParameter("uploadId", uploadId);
      URL presignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest);

      presignedUrlInfos.add(PresignedUrlInfo.builder()
          .partNumber(partNumber)
          .preSignedUrl(presignedUrl.toString())
          .build());
    }
    return InitResultDto.builder()
        .uploadId(uploadId)
        .fileKey(fileKey)
        .presignedUrlInfos(presignedUrlInfos)
        .build();
    } catch (Exception e) {
      log.error("Pre-Signed URL Init 실패 : {}", e.getMessage());
      throw new ServerErrorException(FILE_UPLOAD_FAIL.getMessage());
    }
  }

  private void validateFileSize(@NotNull Long fileSize) {
    long maxSize = 30 * 1024 * 1024L;
    if (fileSize > maxSize) {
      throw new BadRequestException(INVALID_FILE_SIZE.getMessage());
    }
  }

  private String validateFileExtension(@NotBlank String originalFileName, String fileCategory) {
    if (originalFileName == null || !originalFileName.contains(".")) {
      throw new BadRequestException(NO_FILE_EXTENSION.getMessage());
    }

    String extension = originalFileName
        .substring(originalFileName.lastIndexOf('.') + 1)
        .toLowerCase();

    if (!FileCategory.valueOf(fileCategory).getAllowedExtensions().contains(extension)) {
      throw new BadRequestException(INVALID_FILE_EXTENSION_TYPE.getMessage());
    }

    return switch (extension) {
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      case "webp" -> "image/gif";
      case "mp4" -> "video/mp4";
      case "mov" -> "video/quicktime";
      case "webm" -> "video/webm";
      default -> "application/octet-stream";
    };
  }

  private String generateUniqueFileKey(String originalFileName, String fileCategory) {
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    String uniqueId = UUID.randomUUID().toString().substring(0, 16);
    String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
    String fileName = uniqueId + "_" + formattedDate + extension;

    return String.format("%s/%s",
        getDirectory(FileCategory.valueOf(fileCategory)).replaceFirst("^/", ""),
        fileName);
  }

  public String getDirectory(FileCategory fileCategory) {
    return switch (fileCategory) {
      case MEMBER_PROFILE -> MEMBER_PROFILE_DIRECTORY;
      case REVIEW -> REVIEW_DIRECTORY;
      case INFO_FIX_REPORT -> INFO_FIX_REPORT_DIRECTORY;
      case CLAIM_REPORT -> CLAIM_REPORT_DIRECTORY;
    };
  }

  private int calculatePartCount(long fileSize) {
    return (int) Math.ceil((double) fileSize / PART_SIZE);
  }

  private Date getPresignedUrlExpiration() {
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 30;
    expiration.setTime(expTimeMillis);
    return expiration;
  }

  public void uploadComplete(@Valid FileRequest.CompleteCommandDto completeCommandDto) {
    try {
      CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest()
          .withBucketName(bucket)
          .withKey(completeCommandDto.getFileKey())
          .withUploadId(completeCommandDto.getUploadId())
          .withPartETags(completeCommandDto.getParts().stream()
              .map(partInfo -> new PartETag(partInfo.getPartNumber(), partInfo.getETag()))
              .toList());

      amazonS3.completeMultipartUpload(completeMultipartUploadRequest);
    } catch (Exception e) {
      log.error("Pre-Signed URL Complete 실패 : {}", e.getMessage());
      throw new ServerErrorException(FILE_UPLOAD_FAIL.getMessage());
    }
  }
}
