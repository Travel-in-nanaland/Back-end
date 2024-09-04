package com.jeju.nanaland.domain.common.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Post;

public interface PostSearchService {

  Post getPost(Long postId, Category category);
}
