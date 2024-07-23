package com.jeju.nanaland.domain.notice.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeTitleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

  Page<NoticeTitleDto> findNoticeList(Language language, Pageable pageable);
}
