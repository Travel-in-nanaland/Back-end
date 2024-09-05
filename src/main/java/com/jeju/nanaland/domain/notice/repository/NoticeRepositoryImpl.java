package com.jeju.nanaland.domain.notice.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.notice.entity.QNotice.notice;
import static com.jeju.nanaland.domain.notice.entity.QNoticeContent.noticeContent;
import static com.jeju.nanaland.domain.notice.entity.QNoticeTitle.noticeTitle;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.QImageFileDto;
import com.jeju.nanaland.domain.notice.dto.NoticeResponse;
import com.jeju.nanaland.domain.notice.dto.QNoticeResponse_ContentDto;
import com.jeju.nanaland.domain.notice.dto.QNoticeResponse_DetailDto;
import com.jeju.nanaland.domain.notice.dto.QNoticeResponse_TitleDto;
import com.jeju.nanaland.domain.notice.entity.NoticeCategory;
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
  public Page<NoticeResponse.TitleDto> findNoticeList(Language language, Pageable pageable) {
    List<NoticeResponse.TitleDto> resultDto = queryFactory
        .select(new QNoticeResponse_TitleDto(
                notice.id,
                notice.noticeCategory.stringValue(),
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

    resultDto.forEach(
        noticeTitleDto -> {
          String noticeCategory = noticeTitleDto.getNoticeCategory();
          String noticeCategoryByLocale = NoticeCategory.valueOf(noticeCategory)
              .getValueByLocale(language);
          noticeTitleDto.setNoticeCategory(noticeCategoryByLocale);
        }
    );

    JPAQuery<Long> countQuery = queryFactory
        .select(noticeTitle.count())
        .from(noticeTitle)
        .innerJoin(noticeTitle.notice, notice)
        .where(noticeTitle.language.eq(language));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public NoticeResponse.DetailDto getNoticeDetail(Language language, Long id) {
    return queryFactory
        .select(new QNoticeResponse_DetailDto(
            noticeTitle.title,
            notice.createdAt
        ))
        .from(noticeTitle)
        .innerJoin(noticeTitle.notice, notice)
        .where(notice.id.eq(id)
            .and(noticeTitle.language.eq(language)))
        .fetchOne();
  }

  @Override
  public List<NoticeResponse.ContentDto> getNoticeContents(Language language, Long id) {
    return queryFactory
        .select(
            new QNoticeResponse_ContentDto(
                new QImageFileDto(
                    imageFile.originUrl,
                    imageFile.thumbnailUrl
                ),
                noticeContent.content
            )
        )
        .from(noticeContent)
        .innerJoin(noticeContent.noticeTitle, noticeTitle)
        .leftJoin(noticeContent.imageFile, imageFile)
        .where(noticeContent.noticeTitle.notice.id.eq(id)
            .and(noticeContent.noticeTitle.language.eq(language)))
        .fetch();
  }
}
