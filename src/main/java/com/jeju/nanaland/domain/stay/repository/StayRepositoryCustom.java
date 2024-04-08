package com.jeju.nanaland.domain.stay.repository;

import com.jeju.nanaland.domain.stay.dto.StayCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StayRepositoryCustom {

  StayCompositeDto findCompositeDtoById(Long id, String locale);

  Page<StayCompositeDto> searchCompositeDtoByTitle(String title, String locale, Pageable pageable);
}
