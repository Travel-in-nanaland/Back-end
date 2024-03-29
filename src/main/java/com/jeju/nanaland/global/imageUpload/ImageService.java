package com.jeju.nanaland.global.imageUpload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import jakarta.transaction.Transactional;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final AmazonS3Client amazonS3Client;
  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  // 원본 사진 저장
  @Value("/images")
  private String imageDirectory;

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
    String originalName = multipartFile.getOriginalFilename();
    try {
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setContentType(multipartFile.getContentType());
      objectMetadata.setContentLength(multipartFile.getInputStream().available());

      amazonS3Client.putObject(bucketName + imageDirectory, originalName,
          multipartFile.getInputStream(),
          objectMetadata);

      String accessUrl = amazonS3Client.getUrl(bucketName + imageDirectory, originalName)
          .toString();
      return accessUrl;
    } catch (IOException e) {

    }
    return null;
//    Image image = new Image(originalName);
//    String filename = image.getStoredName();

//    try {
//      ObjectMetadata objectMetadata = new ObjectMetadata();
//      objectMetadata.setContentType(multipartFile.getContentType());
//      objectMetadata.setContentLength(multipartFile.getInputStream().available());
//
//      amazonS3Client.putObject(bucketName, filename, multipartFile.getInputStream(), objectMetadata);
//
//      String accessUrl = amazonS3Client.getUrl(bucketName, filename).toString();
//      image.setAccessUrl(accessUrl);
//    } catch(IOException e) {
//
//    }
//
//    imageRepository.save(image);
//
//    return image.getAccessUrl();
//  }
  }

  @Transactional
  public void deleteImage(String filename) {
    amazonS3Client.deleteObject(bucketName, filename);
  }
}
