package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findFestivalCompositeDto(Long id, String locale);
}
