package com.jeju.nanaland.domain.notice.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

  Page<NoticeResponse.PreviewDto> findAllNoticePreviewDtoOrderByCreatedAt(Language language,
      Pageable pageable);

  NoticeResponse.DetailDto findNoticeDetailDto(Language language, Long id);

  List<NoticeResponse.ContentDto> findAllNoticeContentDto(Language language, Long id);
}
