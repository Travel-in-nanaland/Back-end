package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.nature.dto.NatureNatureTransDto;

public interface NatureRepositoryCustom {

  NatureNatureTransDto findNatureNatureTransDtoByIdAndLocale(Long id, String locale);
}
