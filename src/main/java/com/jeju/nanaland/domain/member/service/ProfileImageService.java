package com.jeju.nanaland.domain.member.service;

import static com.jeju.nanaland.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jeju.nanaland.global.exception.ErrorCode.SERVER_ERROR;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.image_upload.S3ImageService;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

  private final MemberRepository memberRepository;
  @Value("${cloud.aws.s3.memberProfileDirectory}")
  private String MEMBER_PROFILE_DIRECTORY;
  private final S3ImageService s3ImageService;
  private final ImageFileRepository imageFileRepository;

  private final List<String> defaultProfile = Arrays.asList("LightPurple.png", "LightGray.png",
      "Gray.png", "DeepBlue.png");
  private final Random random = new Random();

  public ImageFile getRandomProfileImageFile() {
    String selectedProfile = defaultProfile.get(random.nextInt(defaultProfile.size()));
    S3ImageDto s3ImageDto = s3ImageService.getS3Urls(selectedProfile, MEMBER_PROFILE_DIRECTORY);
    return saveS3ImageFile(s3ImageDto);
  }

  public ImageFile saveS3ImageFile(S3ImageDto s3ImageDto) {
    ImageFile imageFile = ImageFile.builder()
        .originUrl(s3ImageDto.getOriginUrl())
        .thumbnailUrl(s3ImageDto.getThumbnailUrl())
        .build();
    return imageFileRepository.save(imageFile);
  }

  @Transactional
  public void setRandomProfileImage(Member member) {
    ImageFile randomProfileImageFile = getRandomProfileImageFile();
    ImageFile profileImageFile = member.getProfileImageFile();
    profileImageFile.updateImageFile(
        randomProfileImageFile.getOriginUrl(),
        randomProfileImageFile.getThumbnailUrl()
    );
  }

  /**
   * 프로필 사진 업데이트
   *
   * @param multipartFile 프로필 사진
   * @param member        회원
   */
  @Transactional
  public  void updateProfileImage(MultipartFile multipartFile, Member member) {
    ImageFile profileImageFile = member.getProfileImageFile();
    try {
      CompletableFuture<S3ImageDto> futureS3ImageDto = s3ImageService.uploadImageToS3(multipartFile,
          true, MEMBER_PROFILE_DIRECTORY);
      S3ImageDto s3ImageDto = futureS3ImageDto.get();
      if (!s3ImageService.isDefaultProfileImage(profileImageFile)) {
        s3ImageService.deleteImageS3(profileImageFile, MEMBER_PROFILE_DIRECTORY);
      }
      profileImageFile.updateImageFile(s3ImageDto.getOriginUrl(), s3ImageDto.getThumbnailUrl());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("스레드 중단", e);
      throw new ServerErrorException(SERVER_ERROR.getMessage());
    } catch (ExecutionException e) {
      log.error("비동기 작업 실행 중 발생하는 예외 발생", e);
      throw new ServerErrorException(SERVER_ERROR.getMessage());
    }
  }

  @Transactional
  public void updateMemberProfileImage(Long memberId, S3ImageDto s3ImageDto) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND.getMessage()));
    ImageFile originImageFile = member.getProfileImageFile();
    originImageFile.updateImageFile(s3ImageDto.getOriginUrl(), s3ImageDto.getThumbnailUrl());
  }
}
