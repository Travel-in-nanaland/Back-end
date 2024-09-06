package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceTypeKeyword;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExperienceRepositoryCustom {

  ExperienceCompositeDto findCompositeDtoById(Long id, Language language);

  Page<ExperienceCompositeDto> searchCompositeDtoByKeyword(String Keyword, Language language,
      Pageable pageable);

  Page<ExperienceThumbnail> findExperienceThumbnails(Language language,
      ExperienceType experienceType, List<ExperienceTypeKeyword> keywordFilterList,
      List<String> addressFilterList, Pageable pageable);

  Set<ExperienceTypeKeyword> getExperienceTypeKeywordSet(Long postId);

  List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language);

  List<Long> findAllIds();

  PostCardDto findPostCardDto(Long postId, Language language);
}
