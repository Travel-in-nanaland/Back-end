package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PopularPostPreviewDto;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.FestivalSearchDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FestivalRepositoryCustom {

  FestivalCompositeDto findCompositeDtoById(Long id, Language locale);

  FestivalCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language locale);

  Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Language locale, Pageable pageable,
      boolean onGoing, List<AddressTag> addressTags);

  Page<FestivalCompositeDto> searchCompositeDtoBySeason(Language locale, Pageable pageable,
      String season);

  Page<FestivalCompositeDto> searchCompositeDtoByMonth(Language locale, Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<AddressTag> addressTags);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);

  List<PopularPostPreviewDto> findAllTop3PopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findRandomPopularPostPreviewDtoByLanguage(Language language,
      List<Long> excludeIds);

  PopularPostPreviewDto findPostPreviewDtoByLanguageAndId(Language language, Long postId);

  Page<FestivalSearchDto> findSearchDtoByKeywordsUnion(List<String> keywords,
      List<AddressTag> addressTags, LocalDate startDate, LocalDate endDate, Language language,
      Pageable pageable);

  Page<FestivalSearchDto> findSearchDtoByKeywordsIntersect(List<String> keywords,
      List<AddressTag> addressTags, LocalDate startDate, LocalDate endDate, Language language,
      Pageable pageable);

  Optional<String> findKoreanAddress(Long postId);
}
