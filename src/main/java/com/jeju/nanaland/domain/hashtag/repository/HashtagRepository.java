package com.jeju.nanaland.domain.hashtag.repository;

import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

  List<Hashtag> findAllByLanguageAndCategoryAndPostId(Language language, Category category,
      Long postId);

}
