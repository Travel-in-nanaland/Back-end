package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;

public interface PostCardService {

  public PostCardDto getPostCardDto(String postId, Language language);
}
