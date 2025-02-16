package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.MarketSearchDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketRepositoryCustom {

  MarketCompositeDto findCompositeDtoById(Long id, Language locale);

  MarketCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language locale);

  Page<MarketThumbnail> findMarketThumbnails(Language locale, List<AddressTag> addressTags,
      Pageable pageable);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);

  List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findRandomPopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId);

  Page<MarketSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      List<AddressTag> addressTags, Language language, Pageable pageable);

  Page<MarketSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      List<AddressTag> addressTags, Language language, Pageable pageable);

  Optional<String> findKoreanAddress(Long postId);
}
