package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findCompositeDtoById(Long id, Locale locale);

  Page<FestivalCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
      Pageable pageable);

  Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Locale locale, Pageable pageable,
      boolean onGoing);

  Page<FestivalCompositeDto> searchCompositeDtoBySeason(Locale locale, Pageable pageable,
      LocalDate startDate, LocalDate endDate, int currentYear);

  Page<FestivalCompositeDto> searchCompositeDtoByMonth(Locale locale, Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<String> addressFilterList);
}
