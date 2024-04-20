package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketRepositoryCustom {

  MarketCompositeDto findCompositeDtoById(Long id, Locale locale);

  Page<MarketResponse.MarketThumbnail> findMarketThumbnails(Locale locale, Pageable pageable);

  Page<MarketCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
      Pageable pageable);
}
