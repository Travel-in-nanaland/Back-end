package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketRepositoryCustom {

  MarketCompositeDto findCompositeDtoById(Long id, Locale locale);

  Page<MarketCompositeDto> findMarketThumbnails(Locale locale, List<String> addressFilterList,
      Pageable pageable);

  Page<MarketCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable);
}
