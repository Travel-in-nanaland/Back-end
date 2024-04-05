package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;

public interface MarketRepositoryCustom {

  MarketCompositeDto findMarketCompositeDto(Long id, String locale);
}
