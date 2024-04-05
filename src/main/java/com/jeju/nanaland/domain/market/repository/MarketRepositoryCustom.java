package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.market.dto.MarketMarketTransDto;

public interface MarketRepositoryCustom {

  MarketMarketTransDto findMarketMarketTransDtoByIdAndLocale(Long id, String locale);
}
