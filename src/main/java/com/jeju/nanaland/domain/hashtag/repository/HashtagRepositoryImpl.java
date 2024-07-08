package com.jeju.nanaland.domain.hashtag.repository;

import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.hashtag.entity.QKeyword.keyword;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<String> findKeywords(Long postId, Category category, Language language) {
    return queryFactory
        .select(keyword.content)
        .from(hashtag)
        .innerJoin(hashtag.keyword, keyword)
        .where(hashtag.post.id.eq(postId)
            .and(hashtag.language.eq(language)))
        .fetch();
  }
}
