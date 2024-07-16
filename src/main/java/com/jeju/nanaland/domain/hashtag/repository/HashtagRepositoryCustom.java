package com.jeju.nanaland.domain.hashtag.repository;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import java.util.List;

public interface HashtagRepositoryCustom {

  List<String> findKeywords(Long postId, Category category, Language language);
}
