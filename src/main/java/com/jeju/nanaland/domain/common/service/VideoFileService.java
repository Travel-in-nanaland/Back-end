package com.jeju.nanaland.domain.common.service;

import static com.jeju.nanaland.global.exception.ErrorCode.*;

import com.jeju.nanaland.domain.common.entity.VideoFile;
import com.jeju.nanaland.domain.common.repository.VideoFileRepository;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3VideoService;
import com.jeju.nanaland.global.image_upload.dto.S3VideoDto;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoFileService {

  private final S3VideoService s3VideoService;
  private final VideoFileRepository videoFileRepository;
  private final FileService fileService;

  public VideoFile saveS3VideoFile(S3VideoDto s3VideoDto) {
    VideoFile videoFile = VideoFile.builder()
        .originUrl(s3VideoDto.getOriginUrl())
        .build();
    return videoFileRepository.save(videoFile);
  }

  // S3에 저장될 경로 지정
  public VideoFile uploadAndSaveVideoFile(File file, String directory) {
    try {
      MultipartFile multipartFile = fileService.convertFileToMultipartFile(file);
      CompletableFuture<S3VideoDto> futureVideoDto = s3VideoService.uploadVideoToS3(multipartFile, directory);
      S3VideoDto s3VideoDto = futureVideoDto.join();
      return saveS3VideoFile(s3VideoDto);
    } catch (IOException e) {
      log.error("파일 업로드 오류: ", e);
      throw new ServerErrorException(FILE_FAIL_ERROR.getMessage());
    }
  }
}
