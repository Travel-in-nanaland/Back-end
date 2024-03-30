package com.jeju.nanaland.global.imageUpload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jeju.nanaland.global.exception.UnsupportedFileFormatException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

  private final AmazonS3Client amazonS3Client;
  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  // 원본 사진 저장
  @Value("/images")
  private String imageDirectory;

  public void verifyExtension(MultipartFile multipartFile) throws UnsupportedFileFormatException {
    String contentType = multipartFile.getContentType();

    // 확장자가 jpeg, png인 파일들만 받아서 처리
    if (ObjectUtils.isEmpty(contentType) | (!contentType.contains("image/jpeg")
        & !contentType.contains("image/png"))) {
      throw new UnsupportedFileFormatException();
    }
  }
//  @Transactional
//  public List<String> saveImages(ImageSaveDto saveDto) {
//    List<String> resultList = new ArrayList<>();
//
//    for(MultipartFile multipartFile : saveDto.getImages()) {
//      String value = saveImage(multipartFile);
//      resultList.add(value);
//    }
//
//    return resultList;
//  }

  @Transactional
  public String saveImage(MultipartFile multipartFile) {

    //이미지 파일인지 검증
    verifyExtension(multipartFile);

    //uuid_originalFilename 로 s3에 업로드할 파일 이름 설정
    UUID uuid = UUID.randomUUID();
    String uploadImageName = uuid + "_" + multipartFile.getOriginalFilename();

    try {
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setContentType(multipartFile.getContentType());
      objectMetadata.setContentLength(multipartFile.getInputStream().available());

      //S3에 사진 올리기
      amazonS3Client.putObject(bucketName + imageDirectory, uploadImageName,
          multipartFile.getInputStream(),
          objectMetadata);

      //사진 저장 경로 받기

    } catch (IOException e) {

    }
    String accessUrl = amazonS3Client.getUrl(bucketName + imageDirectory, uploadImageName)
        .toString();
    return accessUrl;
//    imageRepository.save(image);
//
//    return image.getAccessUrl();
//  }
  }

  @Transactional
  public void deleteImage(String filename) {
    // imageDirectory는 원본 image파일만 삭제하는 것
    // 실제로 사용할 때는 thumbnail image도 삭제하는 로직 작성해야함.
    amazonS3Client.deleteObject(bucketName + imageDirectory, filename);
  }
}
