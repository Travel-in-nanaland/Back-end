package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.NatureResponse.PreviewDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NatureRepositoryCustom {

  NatureCompositeDto findNatureCompositeDto(Long id, Language locale);

  NatureCompositeDto findNatureCompositeDtoWithPessimisticLock(Long id, Language locale);

  Page<NatureCompositeDto> searchCompositeDtoByKeyword(String keyword, Language locale,
      Pageable pageable);

  Page<PreviewDto> findAllNaturePreviewDtoOrderByPriorityAndCreatedAtDesc(Language locale,
      List<AddressTag> addressTags,
      String keyword, Pageable pageable);

  PostPreviewDto findPostPreviewDto(Long postId, Language language);
}
