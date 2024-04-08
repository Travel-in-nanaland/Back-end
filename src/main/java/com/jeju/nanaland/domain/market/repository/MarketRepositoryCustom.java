package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketRepositoryCustom {

  MarketCompositeDto findCompositeDtoById(Long id, String locale);

  Page<MarketCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable);
}
