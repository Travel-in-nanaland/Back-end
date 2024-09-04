package com.jeju.nanaland.domain.favorite.dto;

import com.jeju.nanaland.domain.common.dto.PostCardDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class FavoritePostCardDto extends PostCardDto {

  public FavoritePostCardDto(PostCardDto postCardDto) {
    super(postCardDto.getId(),
        postCardDto.getTitle(),
        postCardDto.getFirstImage().getOriginUrl(),
        postCardDto.getFirstImage().getThumbnailUrl());
  }
}
