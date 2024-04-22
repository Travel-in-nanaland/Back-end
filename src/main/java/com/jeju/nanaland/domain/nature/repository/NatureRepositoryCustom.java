package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NatureRepositoryCustom {

  NatureCompositeDto findCompositeDtoById(Long id, Locale locale);

  Page<NatureCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
      Pageable pageable);

  Page<NatureCompositeDto> findNatureThumbnails(Locale locale, String addressFilter,
      Pageable pageable);
}
