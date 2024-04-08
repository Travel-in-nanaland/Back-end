package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NatureRepositoryCustom {

  NatureCompositeDto findCompositeDtoById(Long id, String locale);

  Page<NatureCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable);
}
