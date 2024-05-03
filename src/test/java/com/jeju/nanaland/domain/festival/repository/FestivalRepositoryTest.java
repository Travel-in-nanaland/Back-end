package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.entity.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
class FestivalRepositoryTest {

  @Autowired
  FestivalRepository festivalRepository;

  @Test
  @DisplayName("축제 검색")
  void searchFestivalTest() {
    Pageable pageable = PageRequest.of(0, 12);
    festivalRepository.searchCompositeDtoByKeyword("쇼핑", Locale.KOREAN, pageable);
  }
}