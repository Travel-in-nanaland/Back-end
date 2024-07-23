package com.jeju.nanaland.domain.notice.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeContentDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeDetailDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeTitleDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

  Page<NoticeTitleDto> findNoticeList(Language language, Pageable pageable);

  NoticeDetailDto getNoticeDetail(Language language, Long id);

  List<NoticeContentDto> getNoticeContents(Language language, Long id);
}
