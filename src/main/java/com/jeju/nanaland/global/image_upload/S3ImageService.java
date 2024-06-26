package com.jeju.nanaland.global.image_upload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.exception.UnsupportedFileFormatException;
import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import jakarta.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

  private final AmazonS3Client amazonS3Client;
  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  // 원본 사진 저장
  @Value("/images")
  private String imageDirectory;

  @Value("/thumbnail_images")
  private String thumbnailDirectory;

  @Value("thumbnail_")
  private String thumbnailPrefix;

  public void verifyExtension(MultipartFile multipartFile) throws UnsupportedFileFormatException {
    String contentType = multipartFile.getContentType();

    if (ObjectUtils.isEmpty(contentType) ||
        (!contentType.contains("image/jpeg") && !contentType.contains("image/png")
            && !contentType.contains("image/jpg") && !contentType.contains("image/webp"))) {
      throw new UnsupportedFileFormatException();
    }
  }

  /*
  TODO
  dto 생성하면 인풋 수정.
   */
  @Transactional
  public List<S3ImageDto> uploadAndSaveImages(List<MultipartFile> multipartFileList,
      boolean autoThumbnail, String directory) throws IOException {
    List<S3ImageDto> imageFileList = new ArrayList<>();
    for (MultipartFile multipartFile : multipartFileList) {
      imageFileList.add(uploadImageToS3(multipartFile, autoThumbnail, directory));
    }
    return imageFileList;
  }

  private String generateUniqueFileName(String extension) {
    // 오늘 날짜 yyMMdd 포맷으로 string 타입 생성
    String uniqueId = UUID.randomUUID().toString().substring(0, 16);
    String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
    return uniqueId + "_" + formattedDate + extension;
  }

  @Transactional
  public S3ImageDto uploadOriginImageToS3(MultipartFile multipartFile, boolean autoThumbnail)
      throws IOException {
    return uploadImageToS3(multipartFile, autoThumbnail, imageDirectory);
  }

  @Transactional
  public S3ImageDto uploadImageToS3(MultipartFile multipartFile, boolean autoThumbnail,
      String directory)
      throws IOException {

    //이미지 파일인지 검증
    verifyExtension(multipartFile);
    String originalFileName = multipartFile.getOriginalFilename();

    //uuid_originalFilename 로 s3에 업로드할 파일 이름 설정 (파일명이 한글일 경우 동작 안해서 uuid 자체로 파일명 수정)
    //확장자 추출
    String extension = null;
    if (originalFileName != null) {
      extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
    }

    //최종 파일 이름 => uuid16자리 + _ + yyMMdd
    String uploadImageName = generateUniqueFileName(extension);

    //S3에 사진 올리기
    String originalImageUrl = uploadImage(multipartFile, directory, uploadImageName);

    //true, false로 구분해서 썸네일 만들고 안만들고 동작.
    String thumbnailImageUrl = "";
    if (autoThumbnail) {
      //섬네일 생성 후 저장
      thumbnailImageUrl = makeThumbnailImageAndUpload(multipartFile, uploadImageName);
    }

    return S3ImageDto.builder()
        .originUrl(originalImageUrl)
        .thumbnailUrl(thumbnailImageUrl)
        .build();
  }

  public String uploadImage(MultipartFile multipartFile, String directory, String imageName)
      throws IOException {
    //메타데이터 설정
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());
    objectMetadata.setContentLength(multipartFile.getInputStream().available());
    amazonS3Client.putObject(bucketName + directory, imageName, multipartFile.getInputStream(),
        objectMetadata);
    return amazonS3Client.getUrl(bucketName + directory, imageName).toString();
  }

  // 자동 생성 아닌 직접 썸네일 올릴 때만 사용
  public String uploadImageToDirectory(MultipartFile multipartFile, ObjectMetadata objectMetadata,
      String imageName)
      throws IOException {
    amazonS3Client.putObject(bucketName + imageDirectory, imageName, multipartFile.getInputStream(),
        objectMetadata);
    return amazonS3Client.getUrl(bucketName + imageDirectory, imageName).toString();
  }

  public String makeThumbnailImageAndUpload(MultipartFile multipartFile, String originImageFileName)
      throws IOException {
    String uploadThumbnailImageName = thumbnailPrefix + originImageFileName;
    BufferedImage bufferImage = ImageIO.read(multipartFile.getInputStream());

    //일단 width: 240px, height: 300px으로 섬네일 제작. 기획과 상의 후 크기 수정.
    BufferedImage thumbnailImage = Thumbnails.of(bufferImage).size(240, 300).asBufferedImage();

    ByteArrayOutputStream thumbOutput = new ByteArrayOutputStream();
    String imageType = multipartFile.getContentType();
    if (imageType != null) {
      ImageIO.write(thumbnailImage, imageType.substring(imageType.indexOf("/") + 1), thumbOutput);
    }

    //메타데이터 설정
    ObjectMetadata objectMetadata = new ObjectMetadata();
    byte[] thumbBytes = thumbOutput.toByteArray();
    objectMetadata.setContentLength(thumbBytes.length);
    objectMetadata.setContentType(multipartFile.getContentType());

    //S3에 사진 올리기
    InputStream thumbInput = new ByteArrayInputStream(thumbBytes);
    amazonS3Client.putObject(bucketName + thumbnailDirectory, uploadThumbnailImageName,
        thumbInput, objectMetadata);

    thumbInput.close();
    thumbOutput.close();

    return amazonS3Client.getUrl(bucketName + thumbnailDirectory,
            uploadThumbnailImageName)
        .toString();
  }

  // 이미지 여러 개 삭제 필요시 추후 별도 메서드 생성 예정.
  @Transactional
  public void deleteImageS3(ImageFile imageFile) {
    // 원본 파일 이름 찾기
    String filename = extractFileName(imageFile.getOriginUrl());

    // 원본 이미지 삭제
    amazonS3Client.deleteObject(bucketName + imageDirectory, filename);

    if (amazonS3Client.doesObjectExist(bucketName + thumbnailDirectory,
        thumbnailPrefix + filename)) { // 썸네일 이미지가 있으면
      log.info("섬네일 존재");

      //썸네일 이미지 삭제
      amazonS3Client.deleteObject(bucketName + thumbnailDirectory, thumbnailPrefix + filename);
    }
  }

  public String extractFileName(String accessUrl) {
    String[] parts = accessUrl.split("images/");
    if (parts.length > 1) {
      // "images/" 다음의 부분 추출
      return parts[1];
    } else {
      throw new ServerErrorException("이미지 파일 이름 추출 에러");
    }
  }

  public boolean isDefaultProfileImage(ImageFile profileImageFile) {
    List<String> defaultProfile = Arrays.asList("LightPurple.png", "LightGray.png", "Gray.png",
        "DeepBlue.png");
    String fileName = extractFileName(profileImageFile.getOriginUrl());
    return defaultProfile.contains(fileName);
  }

  public S3ImageDto getS3Urls(String imageName) {
    String originUrl = amazonS3Client.getUrl(bucketName + imageDirectory,
        imageName).toString();
    String thumbnailUrl = amazonS3Client.getUrl(bucketName + thumbnailDirectory,
            thumbnailPrefix + imageName)
        .toString();
    return S3ImageDto.builder()
        .originUrl(originUrl)
        .thumbnailUrl(thumbnailUrl)
        .build();
  }
}
