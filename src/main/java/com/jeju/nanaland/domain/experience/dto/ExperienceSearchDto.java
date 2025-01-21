package com.jeju.nanaland.domain.experience.dto;

import com.jeju.nanaland.domain.common.dto.SearchDto;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceSearchDto extends SearchDto {

  @QueryProjection
  public ExperienceSearchDto(Long id, String title, String originUrl, String thumbnailUrl,
      Long matchedCount,
      LocalDateTime createdAt) {
    super(id, title, originUrl, thumbnailUrl, matchedCount, createdAt);
  }
}
