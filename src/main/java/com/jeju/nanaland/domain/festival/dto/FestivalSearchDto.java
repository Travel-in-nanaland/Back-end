package com.jeju.nanaland.domain.festival.dto;

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
public class FestivalSearchDto extends SearchDto {

  @QueryProjection
  public FestivalSearchDto(Long id, String title, String originUrl, String thumbnailUrl,
      Long matchedCount,
      LocalDateTime createdAt) {
    super(id, title, originUrl, thumbnailUrl, matchedCount, createdAt);
  }
}
