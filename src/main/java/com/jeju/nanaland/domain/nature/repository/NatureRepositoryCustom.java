package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.PreviewDto;
import com.jeju.nanaland.domain.nature.dto.NatureSearchDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NatureRepositoryCustom {

  NatureCompositeDto findNatureCompositeDto(Long id, Language locale);

  NatureCompositeDto findNatureCompositeDtoWithPessimisticLock(Long id, Language locale);

  Page<PreviewDto> findAllNaturePreviewDtoOrderByPriorityAndCreatedAtDesc(Language locale,
      List<AddressTag> addressTags,
      String keyword, Pageable pageable);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);

  List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findRandomPopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId);

  Page<NatureSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      List<AddressTag> addressTags, Language language, Pageable pageable);

  Page<NatureSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      List<AddressTag> addressTags, Language language, Pageable pageable);

  Optional<String> findKoreanAddress(Long postId);
}
