package com.jeju.nanaland.domain.experience.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExperienceSearchDto {

  private Long id;
  private ImageFileDto firstImage;
  private Long matchedCount;
  private LocalDateTime createdAt;

  @QueryProjection
  public ExperienceSearchDto(Long id, String originUrl, String thumbnailUrl, Long matchedCount,
      LocalDateTime createdAt) {
    this.id = id;
    this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    this.matchedCount = matchedCount;
  }

  public void addMatchedCount(Long count) {
    this.matchedCount += count;
  }
}
