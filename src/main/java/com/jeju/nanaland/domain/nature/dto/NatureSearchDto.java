package com.jeju.nanaland.domain.nature.dto;

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
public class NatureSearchDto {

  private Long id;
  private String title;
  private ImageFileDto firstImage;
  private Long matchedCount;
  private LocalDateTime createdAt;

  @QueryProjection
  public NatureSearchDto(Long id, String title, String originUrl, String thumbnailUrl,
      Long matchedCount, LocalDateTime createdAt) {
    this.id = id;
    this.title = title;
    this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    this.matchedCount = matchedCount;
  }

  public void addMatchedCount(Long count) {
    this.matchedCount += count;
  }
}
