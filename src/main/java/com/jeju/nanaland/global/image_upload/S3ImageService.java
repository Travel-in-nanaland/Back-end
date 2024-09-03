package com.jeju.nanaland.global.image_upload;

import static com.jeju.nanaland.global.exception.ErrorCode.EXTRACT_NAME_ERROR;

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

  /**
   * multipartFile의 확장자 검사
   *
   * @param multipartFile 확장자 검사를 받을 파일
   * @throws UnsupportedFileFormatException 확장자가 jpeg, png, jpg, webp가 아닌 경우
   */
  private void verifyExtension(MultipartFile multipartFile) throws UnsupportedFileFormatException {
    String contentType = multipartFile.getContentType();

    if (ObjectUtils.isEmpty(contentType) ||
        (!contentType.contains("image/jpeg") && !contentType.contains("image/png")
            && !contentType.contains("image/jpg") && !contentType.contains("image/webp"))) {
      throw new UnsupportedFileFormatException();
    }
  }

  /**
   * 파일 이름 생성기
   *
   * @param extension 전달받은 확장자
   * @return uuid + "_" + formatted_date + extension -> 랜덤 값 + 생성시간 + 확장자
   */
  private String generateUniqueFileName(String extension) {
    // 오늘 날짜 yyMMdd 포맷으로 string 타입 생성
    String uniqueId = UUID.randomUUID().toString().substring(0, 16);
    String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
    return uniqueId + "_" + formattedDate + extension;
  }

  // TODO 이미지 별 디릭토리 추가?? - Enum으로 디렉토리 관리? 음 근데 코드로 업로드할 일이 있으려나
  //                            이미지 저장을 기본 디렉토리인 /images 로 고정

  /**
   * @param multipartFile s3에 업로드 할 파일
   * @param autoThumbnail 썸네일 생성 여부
   * @return originUrl, thumbnailUrl을 갖는 S3ImageDto
   * @throws IOException multipartFile 문제가 있을 경우
   */
  @Transactional
  public S3ImageDto uploadOriginImageToS3(MultipartFile multipartFile, boolean autoThumbnail)
      throws IOException {
    return uploadImageToS3(multipartFile, autoThumbnail, imageDirectory);
  }

  // TODO 저장 장소를 매개변수로 받음. 위에 uploadOriginImageToS3랑 합쳐야 할듯.

  /**
   * @param multipartFile s3에 업로드 할 파일
   * @param autoThumbnail 썸네일 생성 여부
   * @param directory     S3 저장소 접두사
   * @return originUrl, thumbnailUrl을 갖는 S3ImageDto
   * @throws IOException
   */
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
    String originalImageUrl = uploadOriginImage(multipartFile, directory, uploadImageName);

    //true, false로 구분해서 썸네일 만들고 안만들고 동작.
    String thumbnailImageUrl = originalImageUrl;
    if (autoThumbnail) { // autoThumbnail이 True인 경우 thumbnailImageUrl 갱신
      //섬네일 생성 후 저장
      thumbnailImageUrl = makeThumbnailImageAndUpload(multipartFile, uploadImageName);
    }

    return S3ImageDto.builder()
        .originUrl(originalImageUrl)
        .thumbnailUrl(thumbnailImageUrl)
        .build();
  }

  /**
   * s3에 원본 이미지 저장
   *
   * @param multipartFile s3에 업로드할 이미지
   * @param directory     S3 저장소 접두사
   * @param imageName     저장할 이미지 이름
   * @return 저장된 이미지 URL
   * @throws IOException
   */
  private String uploadOriginImage(MultipartFile multipartFile, String directory, String imageName)
      throws IOException {
    //메타데이터 설정
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());
    objectMetadata.setContentLength(multipartFile.getInputStream().available());
    amazonS3Client.putObject(bucketName + directory, imageName, multipartFile.getInputStream(),
        objectMetadata);
    return amazonS3Client.getUrl(bucketName + directory, imageName).toString();
  }

  /**
   * @param multipartFile
   * @param objectMetadata
   * @param imageName
   * @return
   * @throws IOException
   * @deprecated 직접 썸네일 올리는 경우 없음.
   */
  // 자동 생성 아닌 직접 썸네일 올릴 때만 사용
  public String uploadImageToDirectory(MultipartFile multipartFile, ObjectMetadata objectMetadata,
      String imageName)
      throws IOException {
    amazonS3Client.putObject(bucketName + imageDirectory, imageName, multipartFile.getInputStream(),
        objectMetadata);
    return amazonS3Client.getUrl(bucketName + imageDirectory, imageName).toString();
  }

  /**
   * orignImage resize하여 썸네일 생성 후 s3에 저장
   *
   * @param multipartFile       썸네일 만들기 전 원본 이미지
   * @param originImageFileName 원본 이미지 파일 이름 (썸네일 이름 접두사 + 원본 이미지 파일이름으로 썸네일 생성)
   * @return 저장된 썸네일 url
   * @throws IOException
   */
  private String makeThumbnailImageAndUpload(MultipartFile multipartFile,
      String originImageFileName)
      throws IOException {
    String uploadThumbnailImageName = thumbnailPrefix + originImageFileName;
    BufferedImage bufferImage = ImageIO.read(multipartFile.getInputStream());

    //일단 width: 600px 으로 섬네일 제작. 기획과 상의 후 크기 수정.
    int newHeight = getResizeImageHeight(multipartFile);
    BufferedImage thumbnailImage = Thumbnails.of(bufferImage).size(600, newHeight)
        .asBufferedImage();

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

  /**
   * imageFile 객체로 이미지 S3에서 삭제
   *
   * @param imageFile 삭제할 ImageFile 객체
   */
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

  /**
   * url로 이미지 삭제
   *
   * @param originUrl 저장된 사진 url로 s3에서 이미지 삭제
   */
  @Transactional
  public void deleteImageS3ByOriginUrl(String originUrl) {
    // 원본 파일 이름 찾기
    String filename = extractFileName(originUrl);

    // 원본 이미지 삭제
    amazonS3Client.deleteObject(bucketName + imageDirectory, filename);

    if (amazonS3Client.doesObjectExist(bucketName + thumbnailDirectory,
        thumbnailPrefix + filename)) { // 썸네일 이미지가 있으면
      log.info("섬네일 존재");

      //썸네일 이미지 삭제
      amazonS3Client.deleteObject(bucketName + thumbnailDirectory, thumbnailPrefix + filename);
    }
  }

  // TODO 여기 images/를 기준으로 split?? 해도되나???

  /**
   * @param accessUrl
   * @return
   */
  private String extractFileName(String accessUrl) {
    String[] parts = accessUrl.split("images/");
    if (parts.length > 1) {
      // "images/" 다음의 부분 추출
      return parts[1];
    } else {
      throw new ServerErrorException(EXTRACT_NAME_ERROR.getMessage());
    }
  }

  // TODO 멤버 사진을 디렉토리로 나눈다면 수정 필요. (ImageFileService로 옮기는 것이 좋을 듯)

  /**
   * @param profileImageFile 사용자 프로필 사진
   * @return defaultProfileImage이면 True, 커스텀 프로필 이미지이면 False
   */
  public boolean isDefaultProfileImage(ImageFile profileImageFile) {
    List<String> defaultProfile = Arrays.asList("LightPurple.png", "LightGray.png", "Gray.png",
        "DeepBlue.png");
    String fileName = extractFileName(profileImageFile.getOriginUrl());
    return defaultProfile.contains(fileName);
  }

  // TODO 이것도 디렉토리를 나누다면 수정 필요

  /**
   * @param imageName 이미지 이름
   * @return
   */
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

  /**
   * 썸네일 생성을 위한 이미지 height resize resize된 크기 width: 600px 고정 height: 원본이 가로로 길었다면 450px 고정, 세로로 길면
   * (원본 세로) / (원본 가로) * 600
   *
   * @param file 이미지 원본
   * @return
   * @throws IOException
   */
  private int getResizeImageHeight(MultipartFile file) throws IOException {
    BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

    int originalHeight = bufferedImage.getHeight();
    int originalWidth = bufferedImage.getWidth();
    int aspectRatio = originalHeight / originalWidth;
    if (aspectRatio == 0) {
      return 450;
    } else {
      return 600 * aspectRatio;
    }
  }
}
