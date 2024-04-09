package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;

public interface ExperienceRepositoryCustom {

  ExperienceCompositeDto findExperienceCompositeDto(Long id, Locale locale);
}
