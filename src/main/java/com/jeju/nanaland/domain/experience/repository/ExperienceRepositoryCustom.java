package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExperienceRepositoryCustom {

  ExperienceCompositeDto findCompositeDtoById(Long id, Locale locale);

  Page<ExperienceCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
      Pageable pageable);
}
