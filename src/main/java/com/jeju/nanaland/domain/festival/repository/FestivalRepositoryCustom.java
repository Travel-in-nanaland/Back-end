package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findCompositeDtoById(Long id, String locale);

  Page<FestivalCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable);
}
