package com.jeju.nanaland.domain.common.service;

import static com.jeju.nanaland.global.exception.ErrorCode.SERVER_ERROR;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.member.service.MemberProfileService;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import com.jeju.nanaland.global.util.CustomMultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageFileService {

  private final MemberProfileService memberProfileService;
  @Value("${cloud.aws.s3.memberProfileDirectory}")
  private String MEMBER_PROFILE_DIRECTORY;
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

  // S3에 저장될 경로 지정
  public ImageFile uploadAndSaveImageFile(MultipartFile multipartFile, boolean autoThumbnail,
      String directory) {
    try {
      CompletableFuture<S3ImageDto> futureS3ImageDto = s3ImageService.uploadImageToS3(multipartFile, autoThumbnail, directory);
      S3ImageDto s3ImageDto = futureS3ImageDto.get();
      return saveS3ImageFile(s3ImageDto);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("스레드 중단", e);
      throw new ServerErrorException(SERVER_ERROR.getMessage());
    } catch (ExecutionException e) {
      log.error("비동기 작업 실행 중 발생하는 예외 발생", e);
      throw new ServerErrorException(SERVER_ERROR.getMessage());
    }
  }

  public ImageFile getRandomProfileImageFile() {
    String selectedProfile = defaultProfile.get(random.nextInt(defaultProfile.size()));
    S3ImageDto s3ImageDto = s3ImageService.getS3Urls(selectedProfile, MEMBER_PROFILE_DIRECTORY);
    return saveS3ImageFile(s3ImageDto);
  }

  public List<ImageFileDto> getPostImageFilesByPostIdIncludeFirstImage(Long postId,
      ImageFileDto firstImage) {
    List<ImageFileDto> images = new ArrayList<>();
    images.add(firstImage);
    images.addAll(imageFileRepository.findPostImageFiles(postId));
    return images;
  }

  public void deleteImageFileInS3ByImageFile(ImageFile imageFile, String directoryPath) {
    s3ImageService.deleteImageS3(imageFile, directoryPath);
  }

  @Async("imageUploadExecutor")
  public void uploadMemberProfileImage(Long memberId, File file) {
    try {
      MultipartFile multipartFile = convertFileToMultipartFile(file);
      s3ImageService.uploadImageToS3(multipartFile, true, MEMBER_PROFILE_DIRECTORY)
          .thenAccept(s3ImageDto ->
              memberProfileService.updateMemberProfileImage(memberId, s3ImageDto)
          )
          .exceptionally(e -> {
            log.error("Failed upload profile image", e);
            throw new CompletionException(new ServerErrorException(SERVER_ERROR.getMessage()));
          });
    } catch (IOException e) {
      log.error("Failed to convert file", e);
      CompletableFuture.failedFuture(new ServerErrorException(SERVER_ERROR.getMessage()));
    }
  }

  private MultipartFile convertFileToMultipartFile(File file) throws IOException {
    String contentType = Files.probeContentType(file.toPath());
    if (contentType == null) {
      contentType = "application/octet-stream";
    }
    return new CustomMultipartFile(file, "file", file.getName(), contentType);
  }
}
