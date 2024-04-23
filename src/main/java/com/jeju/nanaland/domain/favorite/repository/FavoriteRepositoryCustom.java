package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.ThumbnailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<ThumbnailDto> findNatureThumbnails(Long memberId, Locale locale, Pageable pageable);
}
