package com.jeju.nanaland.domain.market.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.entity.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MarketRepositoryTest {

  @Autowired
  MarketRepository marketRepository;

  @Test
  @DisplayName("전통시장 검색")
  void searchMarketTest() {
    Pageable pageable = PageRequest.of(0, 12);
    marketRepository.searchCompositeDtoByKeyword("쇼핑", Locale.KOREAN, pageable);
  }
}