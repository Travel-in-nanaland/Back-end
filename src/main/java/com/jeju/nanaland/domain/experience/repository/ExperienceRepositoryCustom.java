package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExperienceRepositoryCustom {

  ExperienceCompositeDto findCompositeDtoById(Long id, String locale);

  Page<ExperienceCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable);
}
