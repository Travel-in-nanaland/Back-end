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
import com.jeju.nanaland.domain.notice.dto.QNoticeResponse_PreviewDto;
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

  /**
   * 공지사항 프리뷰 페이징 조회
   *
   * @param language 언어
   * @param pageable 페이징 정보
   * @return 공지사항 프리뷰 리스트
   */
  @Override
  public Page<NoticeResponse.PreviewDto> findAllNoticePreviewDtoOrderByCreatedAt(Language language, Pageable pageable) {
    List<NoticeResponse.PreviewDto> resultDto = queryFactory
        .select(new QNoticeResponse_PreviewDto(
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

    // 공지사항 카테고리 정보 언어별 처리
    resultDto.forEach(
        noticeTitleDto -> {
          String noticeCategory = noticeTitleDto.getNoticeCategory();
          String noticeCategoryByLocale = NoticeCategory.valueOf(noticeCategory)
              .getValueByLocale(language);
          noticeTitleDto.setNoticeCategory(noticeCategoryByLocale);
        }
    );

    // 총 데이터 수
    JPAQuery<Long> countQuery = queryFactory
        .select(noticeTitle.count())
        .from(noticeTitle)
        .innerJoin(noticeTitle.notice, notice)
        .where(noticeTitle.language.eq(language));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  /**
   * 공지사항 상세 조회
   *
   * @param language 언어
   * @param noticeId 공지사항 ID
   * @return 공지사항 상세 정보
   */
  @Override
  public NoticeResponse.DetailDto findNoticeDetailDto(Language language, Long noticeId) {
    return queryFactory
        .select(new QNoticeResponse_DetailDto(
            noticeTitle.title,
            notice.createdAt
        ))
        .from(noticeTitle)
        .innerJoin(noticeTitle.notice, notice)
        .where(notice.id.eq(noticeId)
            .and(noticeTitle.language.eq(language)))
        .fetchOne();
  }

  /**
   * 공지사항 내용 조회
   *
   * @param language 언어
   * @param noticeId 공지사항 ID
   * @return 공지사항 내용 정보
   */
  @Override
  public List<NoticeResponse.ContentDto> findAllNoticeContentDto(Language language, Long noticeId) {
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
        .where(noticeContent.noticeTitle.notice.id.eq(noticeId)
            .and(noticeContent.noticeTitle.language.eq(language)))
        .fetch();
  }
}
