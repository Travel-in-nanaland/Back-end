package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import java.util.List;

public interface NanaRepositoryCustom {

  List<NanaResponse.ThumbnailDto> findThumbnailDto(Locale locale);
}
