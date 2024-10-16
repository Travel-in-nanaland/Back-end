package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findFestivalCompositeDto(Long id, Language locale);

  Page<FestivalCompositeDto> searchCompositeDtoByKeyword(String keyword, Language locale,
      Pageable pageable);

  Page<FestivalCompositeDto> findAllFestivalCompositDtoOrderByEndDate(Language locale,
      Pageable pageable,
      boolean onGoing, List<String> addressFilters);

  Page<FestivalCompositeDto> findAllFestivalCompositeDtoOrderByEndDate(Language locale,
      Pageable pageable,
      String season);

  Page<FestivalCompositeDto> findAllFestivalCompositeDtoByEndDate(Language locale,
      Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<String> addressFilters);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);
}
