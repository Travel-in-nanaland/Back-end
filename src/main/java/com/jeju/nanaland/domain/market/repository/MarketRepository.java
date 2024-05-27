package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.domain.market.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market, Long>, MarketRepositoryCustom {

}
