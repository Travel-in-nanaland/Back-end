package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NatureCardService implements PostCardService {

  private final NatureRepository natureRepository;

  @Override
  public PostCardDto getPostCardDto(String postId, Language language) {
    return null;
  }
}
