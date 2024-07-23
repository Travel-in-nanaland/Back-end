package com.jeju.nanaland.domain.notice.repository;

import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;
import static com.jeju.nanaland.domain.notice.entity.QNotice.notice;
import static com.jeju.nanaland.domain.notice.entity.QNoticeTitle.noticeTitle;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse.NoticeTitleDto;
import com.jeju.nanaland.domain.notice.dto.QNoticeResponse_NoticeTitleDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<NoticeTitleDto> findNoticeList(Language language, Pageable pageable) {
    List<NoticeTitleDto> resultDto = queryFactory
        .select(new QNoticeResponse_NoticeTitleDto(
                notice.noticeCategory,
                noticeTitle.title,
                notice.createdAt
            )
        )
        .from(noticeTitle)
        .innerJoin(noticeTitle.notice, notice)
        .where(noticeTitle.language.eq(language))
        .orderBy(notice.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(noticeTitle.count())
        .from(nanaTitle)
        .innerJoin(noticeTitle.notice, notice)
        .where(noticeTitle.language.eq(language));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }
}
