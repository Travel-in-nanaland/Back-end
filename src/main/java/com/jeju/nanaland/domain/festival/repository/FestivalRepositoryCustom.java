package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findCompositeDtoById(Long id, Locale locale);

  Page<FestivalCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable);
}
