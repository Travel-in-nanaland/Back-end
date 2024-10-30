package com.jeju.nanaland.domain.common.entity;

import com.jeju.nanaland.global.image_upload.dto.S3ImageDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFile extends BaseEntity {

  @Column(columnDefinition = "VARCHAR(1024)")
  private String thumbnailUrl;

  @Column(nullable = false, columnDefinition = "VARCHAR(1024)")
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
