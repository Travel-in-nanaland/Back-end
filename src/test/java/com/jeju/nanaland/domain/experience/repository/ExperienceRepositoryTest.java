package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Language;
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
class ExperienceRepositoryTest {

  @Autowired
  ExperienceRepository experienceRepository;

  @Test
  @DisplayName("이색체험 검색")
  void searchExperienceTest() {
    Pageable pageable = PageRequest.of(0, 12);
    experienceRepository.searchCompositeDtoByKeyword("쇼핑", Language.KOREAN, pageable);
  }
}