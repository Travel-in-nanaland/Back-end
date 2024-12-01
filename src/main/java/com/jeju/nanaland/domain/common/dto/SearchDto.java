package com.jeju.nanaland.domain.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SearchDto {

  private Long id;
  private String title;
  private ImageFileDto firstImage;
  private Long matchedCount;
  private LocalDateTime createdAt;

  @QueryProjection
  public SearchDto(Long id, String title, String originUrl, String thumbnailUrl,
      Long matchedCount, LocalDateTime createdAt) {
    this.id = id;
    this.title = title;
    this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
    this.matchedCount = matchedCount;
    this.createdAt = createdAt;
  }

  public void addMatchedCount(Long count) {
    this.matchedCount += count;
  }
}
