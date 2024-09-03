package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;

public interface PostCardService {

  PostCardDto getPostCardDto(Long postId, Language language);
}
