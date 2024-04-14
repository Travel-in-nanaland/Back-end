package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import java.util.List;

public interface NanaRepositoryCustom {

  //메인 페이지에서 슬라이드되는 Nana's pick 찾기
  List<NanaResponse.ThumbnailDto> findRecentNanaThumbnailDto(Locale locale);

  //이거 좀 어렵군요...
//  NanaResponse.nanaDetailDto findNanaDetailById(Long id, Locale locale);
}
