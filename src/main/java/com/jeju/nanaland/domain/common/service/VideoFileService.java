package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.entity.VideoFile;
import com.jeju.nanaland.domain.common.repository.VideoFileRepository;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3VideoService;
import com.jeju.nanaland.global.image_upload.dto.S3VideoDto;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VideoFileService {

  private final S3VideoService s3VideoService;
  private final VideoFileRepository videoFileRepository;

  public VideoFile saveS3VideoFile(S3VideoDto s3VideoDto) {
    VideoFile videoFile = VideoFile.builder()
        .originUrl(s3VideoDto.getOriginUrl())
        .build();
    return videoFileRepository.save(videoFile);
  }

  // S3에 저장될 경로 지정
  public VideoFile uploadAndSaveVideoFile(MultipartFile multipartFile, String directory) {
    try {
      S3VideoDto s3VideoDto = s3VideoService.uploadVideoToS3(multipartFile, directory);
      return saveS3VideoFile(s3VideoDto);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServerErrorException(ErrorCode.SERVER_ERROR.getMessage());
    }
  }
}
