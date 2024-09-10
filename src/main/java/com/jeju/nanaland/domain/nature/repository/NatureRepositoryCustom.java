package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostCardDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.PreviewDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NatureRepositoryCustom {

  NatureCompositeDto findNatureCompositeDto(Long id, Language locale);

  Page<NatureCompositeDto> searchCompositeDtoByKeyword(String keyword, Language locale,
      Pageable pageable);

  Page<PreviewDto> findAllNaturePreviewDtoOrderByPriority(Language locale, List<String> addressFilterList,
      String keyword, Pageable pageable);

  PostCardDto findPostCardDto(Long postId, Language language);
}
