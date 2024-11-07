package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findCompositeDtoById(Long id, Language locale);

  FestivalCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language locale);

  Page<FestivalCompositeDto> searchCompositeDtoByKeyword(String keyword, Language locale,
      Pageable pageable);

  Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Language locale, Pageable pageable,
      boolean onGoing, List<AddressTag> addressTags);

  Page<FestivalCompositeDto> searchCompositeDtoBySeason(Language locale, Pageable pageable,
      String season);

  Page<FestivalCompositeDto> searchCompositeDtoByMonth(Language locale, Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<AddressTag> addressTags);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);
}
