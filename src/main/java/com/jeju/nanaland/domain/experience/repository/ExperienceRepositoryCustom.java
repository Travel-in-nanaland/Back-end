package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.dto.ExperienceCompositeDto;
import com.jeju.nanaland.domain.experience.dto.ExperienceResponse.ExperienceThumbnail;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExperienceRepositoryCustom {

  ExperienceCompositeDto findCompositeDtoById(Long id, Language language);

  Page<ExperienceCompositeDto> searchCompositeDtoByKeyword(String Keyword, Language language,
      Pageable pageable);

  Page<ExperienceThumbnail> findExperienceThumbnails(Language language,
      ExperienceType experienceType, List<String> keywordFilterList, List<String> addressFilterList,
      Pageable pageable);
}
