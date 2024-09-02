package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NatureRepositoryTest {

  @Autowired
  NatureRepository natureRepository;

  @Test
  @DisplayName("7대자연 검색")
  void searchNatureTest() {
    Pageable pageable = PageRequest.of(0, 12);
    Page<NatureCompositeDto> result = natureRepository.searchCompositeDtoByKeyword("자연경관",
        Language.KOREAN,
        pageable);
  }
}