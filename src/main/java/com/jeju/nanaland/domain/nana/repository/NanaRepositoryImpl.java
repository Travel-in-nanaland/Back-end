package com.jeju.nanaland.domain.nana.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnailPost;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NanaRepositoryImpl implements NanaRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  //최신순으로 4
  @Override
  public List<NanaResponse.NanaThumbnail> findRecentNanaThumbnailDto(Locale locale) {
    return queryFactory.select(new QNanaResponse_NanaThumbnail(
            nanaTitle.id,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nanaTitle.imageFile, imageFile)
        .where((nanaTitle.language.locale.eq(locale)))
        .orderBy(nanaTitle.createdAt.desc())
        .limit(4L)
        .fetch();
  }

  // 모든 Nana 썸네일 가져오기
  @Override
  public Page<NanaResponse.NanaThumbnail> findAllNanaThumbnailDto(Locale locale,
      Pageable pageable) {
    List<NanaThumbnail> resultDto = queryFactory.select(new QNanaResponse_NanaThumbnail(
            nanaTitle.id,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nanaTitle.imageFile, imageFile)
        .where((nanaTitle.language.locale.eq(locale)))
        .orderBy(nanaTitle.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nanaTitle.count())
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nanaTitle.imageFile, imageFile)
        .where((nanaTitle.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public NanaThumbnailPost findNanaThumbnailPostDto(Long id, Locale locale) {
    return queryFactory
        .select(new QNanaResponse_NanaThumbnailPost(
            nanaTitle.id,
            imageFile.thumbnailUrl,
            nanaTitle.heading
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.imageFile, imageFile)
        .where(nanaTitle.nana.id.eq(id)
            .and(nanaTitle.language.locale.eq(locale)))
        .fetchOne();
  }
}
