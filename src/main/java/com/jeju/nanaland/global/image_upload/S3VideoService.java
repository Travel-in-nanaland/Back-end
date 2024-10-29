package com.jeju.nanaland.global.image_upload;

import static com.jeju.nanaland.global.exception.ErrorCode.FILE_FAIL_ERROR;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.exception.UnsupportedFileFormatException;
import com.jeju.nanaland.global.image_upload.dto.S3VideoDto;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3VideoService {


  private final AmazonS3Client amazonS3Client;
  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public void verifyExtension(MultipartFile multipartFile) throws UnsupportedFileFormatException {
    String contentType = multipartFile.getContentType();

    if (ObjectUtils.isEmpty(contentType) ||
        (!contentType.contains("video/"))) {
      throw new UnsupportedFileFormatException();
    }
  }

  /**
   * 파일 이름 생성기
   *
   * @param originalFileName 파일명
   * @return uuid + "_" + formatted_date + extension -> 랜덤 값 + 생성시간 + 확장자
   */
  private String generateUniqueFileName(String originalFileName) {
    //uuid_originalFilename 로 s3에 업로드할 파일 이름 설정 (파일명이 한글일 경우 동작 안해서 uuid 자체로 파일명 수정)
    //확장자 추출
    String extension = null;
    if (originalFileName != null) {
      extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    // 오늘 날짜 yyMMdd 포맷으로 string 타입 생성
    String uniqueId = UUID.randomUUID().toString().substring(0, 16);
    String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
    return uniqueId + "_" + formattedDate + extension;
  }

  @Async("imageUploadExecutor")
  public CompletableFuture<S3VideoDto> uploadVideoToS3(MultipartFile multipartFile, String directory) {

    return CompletableFuture.supplyAsync(() -> {
      try {
        // 동영상 파일인지 검증
        verifyExtension(multipartFile);

        //최종 파일 이름 => uuid16자리 + _ + yyMMdd
        String uploadVideoName = generateUniqueFileName(multipartFile.getOriginalFilename());

        //S3에 동영상 올리기
        String originalVideoUrl = uploadVideo(multipartFile, directory, uploadVideoName);

        return S3VideoDto.builder()
            .originUrl(originalVideoUrl)
            .build();
      } catch (Exception e) {
        log.error("파일 업로드 오류: {}", e.getMessage());
        throw new ServerErrorException(FILE_FAIL_ERROR.getMessage());
      }
    });
  }

  public String uploadVideo(MultipartFile multipartFile, String directory, String videoName)
      throws IOException {
    //메타데이터 설정
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());
    objectMetadata.setContentLength(multipartFile.getInputStream().available());
    amazonS3Client.putObject(bucketName + directory, videoName, multipartFile.getInputStream(),
        objectMetadata);
    return amazonS3Client.getUrl(bucketName + directory, videoName).toString();
  }
}
