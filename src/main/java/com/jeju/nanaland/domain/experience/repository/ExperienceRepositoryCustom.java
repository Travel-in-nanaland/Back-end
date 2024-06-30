package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExperienceRepositoryCustom {

  ExperienceCompositeDto findCompositeDtoById(Long id, Language locale);

  Page<ExperienceCompositeDto> searchCompositeDtoByKeyword(String Keyword, Language locale,
      Pageable pageable);
}
