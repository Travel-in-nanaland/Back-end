package com.jeju.nanaland.domain.restaurant.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {

  Page<RestaurantThumbnail> findRestaurantThumbnails(Language language,
      List<RestaurantTypeKeyword> keywordFilter, List<String> addressFilter, Pageable pageable);

  RestaurantCompositeDto findCompositeDtoById(Long postId, Language language);

  Set<RestaurantTypeKeyword> getRestaurantTypeKeywordSet(Long postId);

  List<RestaurantMenuDto> getRestaurantMenuList(Long postId, Language language);

  Page<RestaurantCompositeDto> searchCompositeDtoByKeyword(String keyword, Language language,
      Pageable pageable);

  List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language);

  List<Long> findAllIds();
}
