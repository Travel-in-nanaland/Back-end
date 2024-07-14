package com.jeju.nanaland.domain.restaurant.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {

  Page<RestaurantThumbnail> findRestaurantThumbnails(Language language,
      List<RestaurantTypeKeyword> keywordFilter, List<String> addressFilter, Pageable pageable);
}
