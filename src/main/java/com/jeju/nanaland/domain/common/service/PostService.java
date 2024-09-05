package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.common.entity.Post;

public interface PostService {

  Post getPost(Long postId, Category category);

  PostCardDto getPostCardDto(Long postId, Category category, Language language);
}
