package com.jeju.nanaland.global.imageUpload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.repository.ImageFileRepository;
import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.exception.UnsupportedFileFormatException;
import jakarta.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
public class ImageService {

  private final ImageFileRepository imageFileRepository;

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

    // 확장자가 jpeg, png인 파일들만 받아서 처리
    if (ObjectUtils.isEmpty(contentType) | (!contentType.contains("image/jpeg")
        & !contentType.contains("image/png"))) {
      throw new UnsupportedFileFormatException();
    }
  }

  /*
  TODO
  dto 생성하면 인풋 수정.
   */
  @Transactional
  public List<ImageFile> saveImages(List<MultipartFile> multipartFileList) throws IOException {
    List<ImageFile> imageFileList = new ArrayList<>();
    for (MultipartFile multipartFile : multipartFileList) {
      imageFileList.add(saveImage(multipartFile));
    }
    return imageFileList;
  }

  @Transactional
  public ImageFile saveImage(MultipartFile multipartFile) throws IOException {

    //이미지 파일인지 검증
    verifyExtension(multipartFile);
    String originalFileName = multipartFile.getOriginalFilename();

    //uuid_originalFilename 로 s3에 업로드할 파일 이름 설정 (파일명이 한글일 경우 동작 안해서 uuid 자체로 파일명 수정)
    // 확장자 추출
    String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
    UUID uuid = UUID.randomUUID();
    String uploadImageName = uuid.toString().substring(0, 16) + extension;

    //메타데이터 설정
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());
    objectMetadata.setContentLength(multipartFile.getInputStream().available());

    //S3에 사진 올리기
    amazonS3Client.putObject(bucketName + imageDirectory, uploadImageName,
        multipartFile.getInputStream(),
        objectMetadata);

    String originalImageUrl = amazonS3Client.getUrl(bucketName + imageDirectory, uploadImageName)
        .toString();

    /**
     * 모든 이미지가 썸네일이 필요한가?? -> 아닌 것도 있다면 true, flase로 구분해서 썸네일 만들고 안만들고 동작.
     */
    //섬네일 생성 후 저장
    String thumbnailImageUrl = uploadThumbnailImage(multipartFile, uploadImageName);

    ImageFile imageFile = ImageFile.builder()
        .originUrl(originalImageUrl)
        .thumbnailUrl(thumbnailImageUrl)
        .build();

    return imageFileRepository.save(imageFile);

  }

  public String uploadThumbnailImage(MultipartFile multipartFile, String originImageFileName)
      throws IOException {
    String uploadThumbnailImageName = thumbnailPrefix + originImageFileName;
    BufferedImage bufferImage = ImageIO.read(multipartFile.getInputStream());

    //일단 width: 240px, height: 300px으로 섬네일 제작. 기획과 상의 후 크기 수정.
    BufferedImage thumbnailImage = Thumbnails.of(bufferImage).size(240, 300).asBufferedImage();

    ByteArrayOutputStream thumbOutput = new ByteArrayOutputStream();
    String imageType = multipartFile.getContentType();
    ImageIO.write(thumbnailImage, imageType.substring(imageType.indexOf("/") + 1), thumbOutput);

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
  public void deleteImage(ImageFile imageFile) {
    // 원본 파일 이름 찾기
    String filename = extractFileName(imageFile.getOriginUrl());

    // 원본 이미지 삭제
    amazonS3Client.deleteObject(bucketName + imageDirectory, filename);

    if (amazonS3Client.doesObjectExist(bucketName + thumbnailDirectory,
        thumbnailPrefix + filename)) { // 썸네일 이미지가 있으면
      log.info("섬네일 존재");

      //썸네일 이미지 삭제
      amazonS3Client.deleteObject(bucketName + thumbnailDirectory, thumbnailPrefix + filename);
      imageFileRepository.delete(imageFile);
    }
  }

  public String extractFileName(String accessUrl) {
    String extractedString = "";
    String[] parts = accessUrl.split("images/");
    if (parts.length > 1) {
      // "images/" 다음의 부분 추출
      extractedString = parts[1];
    } else {
      throw new ServerErrorException("이미지 파일 이름 추출 에러");
    }
    return extractedString;
  }
}
