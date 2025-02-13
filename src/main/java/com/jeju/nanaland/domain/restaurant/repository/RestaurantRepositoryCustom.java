package com.jeju.nanaland.domain.restaurant.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantSearchDto;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {

  Page<RestaurantThumbnail> findRestaurantThumbnails(Language language,
      List<RestaurantTypeKeyword> keywordFilter, List<AddressTag> addressTags, Pageable pageable);

  RestaurantCompositeDto findCompositeDtoById(Long postId, Language language);

  RestaurantCompositeDto findCompositeDtoByIdWithPessimisticLock(Long postId, Language language);

  Set<RestaurantTypeKeyword> getRestaurantTypeKeywordSet(Long postId);

  Set<RestaurantTypeKeyword> getRestaurantTypeKeywordSetWithPessimisticLock(Long postId);

  List<RestaurantMenuDto> getRestaurantMenuList(Long postId, Language language);

  List<RestaurantMenuDto> getRestaurantMenuListWithPessimisticLock(Long postId, Language language);

  List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language);

  List<Long> findAllIds();

  PostPreviewDto findPostPreviewDto(Long postId, Language language);

  List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findRandomPopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId);

  Page<RestaurantSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords, Language language,
      Pageable pageable);

  Page<RestaurantSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      Language language, Pageable pageable);

  Optional<String> findKoreanAddress(Long postId);
}
