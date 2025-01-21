package com.jeju.nanaland.domain.nana.dto;

import com.jeju.nanaland.domain.common.dto.SearchDto;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NanaSearchDto extends SearchDto {

  @QueryProjection
  public NanaSearchDto(Long id, String title, String originUrl, String thumbnailUrl,
      Long matchedCount,
      LocalDateTime createdAt) {
    super(id, title, originUrl, thumbnailUrl, matchedCount, createdAt);
  }

  public void setMatchedCount(Long matchedCount) {
    this.matchedCount = matchedCount;
  }
}
