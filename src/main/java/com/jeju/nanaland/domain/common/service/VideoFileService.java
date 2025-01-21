package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.entity.VideoFile;
import com.jeju.nanaland.domain.common.repository.VideoFileRepository;
import com.jeju.nanaland.global.file.service.FileUploadService;
import com.jeju.nanaland.global.image_upload.dto.S3VideoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoFileService {

  private final VideoFileRepository videoFileRepository;
  private final FileUploadService fileUploadService;

  public VideoFile saveS3VideoFile(S3VideoDto s3VideoDto) {
    VideoFile videoFile = VideoFile.builder()
        .originUrl(s3VideoDto.getOriginUrl())
        .build();
    return videoFileRepository.save(videoFile);
  }

  // S3에 저장될 경로 지정
  public VideoFile getAndSaveVideoFile(String fileKey) {
    S3VideoDto s3VideoDto = fileUploadService.getCloudVideoUrls(fileKey);
    return saveS3VideoFile(s3VideoDto);
  }
}
