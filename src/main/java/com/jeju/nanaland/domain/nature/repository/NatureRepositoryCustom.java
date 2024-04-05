package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;

public interface NatureRepositoryCustom {

  NatureCompositeDto findNatureCompositeDto(Long id, String locale);
}
