package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.experience.dto.ExperienceExperienceTransDto;

public interface ExperienceRepositoryCustom {

  ExperienceExperienceTransDto getExperienceExperienceTransDtoByIdAndLocale(Long id, String locale);
}
