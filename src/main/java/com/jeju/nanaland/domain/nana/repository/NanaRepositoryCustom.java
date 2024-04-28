package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NanaRepositoryCustom {

  //메인 페이지에서 슬라이드되는 Nana's pick 찾기
  List<NanaResponse.NanaThumbnail> findRecentNanaThumbnailDto(Locale locale);

  //나나 pick 눌렀을 때 나올 모든 썸네일
  Page<NanaThumbnail> findAllNanaThumbnailDto(Locale locale, Pageable pageable);
  //이거 좀 어렵군요...
//  NanaResponse.nanaDetailDto findNanaDetailById(Long id, Locale locale);

  Page<NanaThumbnail> searchNanaThumbnailDtoByKeyword(String keyword, Locale locale,
      Pageable pageable);
}
