package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
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

  ExperienceCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language language);

  Page<ExperienceCompositeDto> searchCompositeDtoByKeyword(String keyword, Language language,
      Pageable pageable);

  Page<ExperienceThumbnail> findExperienceThumbnails(Language language,
      ExperienceType experienceType, List<ExperienceTypeKeyword> keywordFilterList,
      List<AddressTag> addressTags, Pageable pageable);

  Set<ExperienceTypeKeyword> getExperienceTypeKeywordSet(Long postId);

  Set<ExperienceTypeKeyword> getExperienceTypeKeywordSetWithWithPessimisticLock(Long postId);

  List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language);

  List<Long> findAllIds();

  PostPreviewDto findPostPreviewDto(Long postId, Language language);

  List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language);

  PopularPostPreviewDto findRandomPopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId);


}
