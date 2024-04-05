package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.festival.dto.FestivalFestivalTransDto;

public interface FestivalRepositoryCustom {

  FestivalFestivalTransDto findFestivalFestivalTransDtoByIdAndLocale(Long id, String locale);
}
