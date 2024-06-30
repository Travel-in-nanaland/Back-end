package com.jeju.nanaland.domain.nana.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.nana.entity.QNana.nana;
import static com.jeju.nanaland.domain.nana.entity.QNanaContent.nanaContent;
import static com.jeju.nanaland.domain.nana.entity.QNanaTitle.nanaTitle;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nana.dto.NanaResponse;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.NanaResponse.NanaThumbnailPost;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnail;
import com.jeju.nanaland.domain.nana.dto.QNanaResponse_NanaThumbnailPost;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
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
            nana.id,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.nanaTitleImageFile, imageFile)
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
            nana.id,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading
        ))
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.nanaTitleImageFile, imageFile)
        .where((nanaTitle.language.locale.eq(locale)))
        .orderBy(nanaTitle.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nanaTitle.count())
        .from(nanaTitle)
        .leftJoin(nanaTitle.nana, nana)
        .leftJoin(nana.nanaTitleImageFile, imageFile)
        .where((nanaTitle.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<NanaThumbnail> searchNanaThumbnailDtoByKeyword(String keyword, Locale locale,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, locale);

    List<NanaThumbnail> resultDto = queryFactory.selectDistinct(new QNanaResponse_NanaThumbnail(
            nana.id,
            imageFile.thumbnailUrl,
            nana.version,
            nanaTitle.heading,
            nanaTitle.subHeading
        ))
        .from(nana)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.locale.eq(locale)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(nana.nanaTitleImageFile, imageFile)
        .where(nanaTitle.heading.contains(keyword)
            .or(nanaContent.title.contains(keyword))
            .or(nanaContent.content.contains(keyword))
            .or(nana.id.in(idListContainAllHashtags)))
        .orderBy(nanaTitle.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nana.id.countDistinct())
        .from(nana)
        .leftJoin(nanaTitle).on(nanaTitle.nana.eq(nana).and(nanaTitle.language.locale.eq(locale)))
        .leftJoin(nanaContent).on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(nana.nanaTitleImageFile, imageFile)
        .where(nanaTitle.heading.contains(keyword)
            .or(nanaContent.title.contains(keyword))
            .or(nanaContent.content.contains(keyword))
            .or(nana.id.in(idListContainAllHashtags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  public NanaThumbnailPost findNanaThumbnailPostDto(Long id, Locale locale) {
    return queryFactory
        .select(new QNanaResponse_NanaThumbnailPost(
            nanaTitle.id,
            imageFile.thumbnailUrl,
            nanaTitle.heading
        ))
        .from(nanaTitle)
        .leftJoin(nana.nanaTitleImageFile, imageFile)
        .where(nanaTitle.nana.id.eq(id)
            .and(nanaTitle.language.locale.eq(locale)))
        .fetchOne();
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Locale locale) {
    return queryFactory
        .select(nana.id)
        .from(nana)
        .leftJoin(nanaTitle)
        .on(nanaTitle.nana.eq(nana).and(nanaTitle.language.locale.eq(locale)))
        .leftJoin(nanaContent)
        .on(nanaContent.nanaTitle.eq(nanaTitle))
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(nanaContent.id)
            .and(hashtag.category.eq(Category.NANA_CONTENT))
            .and(hashtag.language.locale.eq(locale)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(nana.id)
        .having(nana.id.count().eq(splitKeyword(keyword).stream().count()))
        .fetch();
  }

  private List<String> splitKeyword(String keyword) {
    String[] tokens = keyword.split("\\s+");
    List<String> tokenList = new ArrayList<>();

    for (String token : tokens) {
      tokenList.add(token.trim());
    }
    return tokenList;
  }
}
