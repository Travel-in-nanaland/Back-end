package com.jeju.nanaland.domain.hashtag.service;

import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.hashtag.entity.Hashtag;
import com.jeju.nanaland.domain.hashtag.entity.Keyword;
import com.jeju.nanaland.domain.hashtag.repository.HashtagRepository;
import com.jeju.nanaland.domain.hashtag.repository.KeywordRepository;
import com.jeju.nanaland.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagService {

  private final KeywordRepository keywordRepository;
  private final HashtagRepository hashtagRepository;

  @Transactional
  public void registerHashtag(List<String> stringKeywordList, Language language, Category category,
      Long postId) {
    List<Hashtag> hashtagList = new ArrayList<>();

    for (String stringKeyword : stringKeywordList) {
      Keyword keyword;
      // 존재하지 않는 keyword일 경우 -> keyword 생성
      if (!existKeyword(stringKeyword)) {
        keyword = keywordRepository.save(Keyword.builder()
            .content(stringKeyword)
            .build());
      } else {
        keyword = keywordRepository.findByContent(stringKeyword)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 keyword 입니다."));

      }
      //Hashtag 생성
      hashtagList.add(Hashtag.builder()
          .category(category)
          .postId(postId)
          .language(language)
          .keyword(keyword)
          .build());

    }

    hashtagRepository.saveAll(hashtagList);
  }

  private boolean existKeyword(String content) {
    return keywordRepository.existsByContent(content);
  }
}
