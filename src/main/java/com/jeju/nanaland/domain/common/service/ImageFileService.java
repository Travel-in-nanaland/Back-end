package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageFileService {

  private final S3ImageService s3ImageService;
  private final ImageFileRepository imageFileRepository;

  private final List<String> defaultProfile = Arrays.asList("LightPurple.png", "LightGray.png",
      "Gray.png", "DeepBlue.png");
  private final Random random = new Random();

  public ImageFile saveS3ImageFile(S3ImageDto s3ImageDto) {
    ImageFile imageFile = ImageFile.builder()
        .originUrl(s3ImageDto.getOriginUrl())
        .thumbnailUrl(s3ImageDto.getThumbnailUrl())
        .build();
    return imageFileRepository.save(imageFile);
  }

  public ImageFile uploadAndSaveImageFile(MultipartFile multipartFile, boolean autoThumbnail) {
    try {
      S3ImageDto s3ImageDto = s3ImageService.uploadOriginImageToS3(multipartFile, autoThumbnail);
      return saveS3ImageFile(s3ImageDto);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServerErrorException(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
  }

  public ImageFile getRandomProfileImageFile() {
    String selectedProfile = defaultProfile.get(random.nextInt(defaultProfile.size()));
    S3ImageDto s3ImageDto = s3ImageService.getS3Urls(selectedProfile);
    return saveS3ImageFile(s3ImageDto);
  }
}
