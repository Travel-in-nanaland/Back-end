package com.jeju.nanaland.domain.common.entity;

import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String thumbnailUrl;

  @Column(nullable = false)
  private String originUrl;

  @Builder
  public ImageFile(String thumbnailUrl, String originUrl) {
    this.thumbnailUrl = thumbnailUrl;
    this.originUrl = originUrl;
  }

  public void updateImageFile(S3ImageDto s3ImageDto) {
    this.originUrl = s3ImageDto.getOriginUrl();
    this.thumbnailUrl = s3ImageDto.getThumbnailUrl();
  }
}
