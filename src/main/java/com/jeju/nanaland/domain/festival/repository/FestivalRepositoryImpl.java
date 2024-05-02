package com.jeju.nanaland.domain.festival.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalCompositeDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class FestivalRepositoryImpl implements FestivalRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public FestivalCompositeDto findCompositeDtoById(Long id, Locale locale) {
    return queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            language.locale,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.id.eq(id).and(festivalTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, locale);

    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language.locale,
            festivalTrans.title,
            festivalTrans.content,
            festivalTrans.address,
            festivalTrans.addressTag,
            festivalTrans.time,
            festivalTrans.intro,
            festivalTrans.fee,
            festival.startDate,
            festival.endDate,
            festival.season
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .where(festivalTrans.title.contains(keyword)
            .or(festivalTrans.addressTag.contains(keyword))
            .or(festivalTrans.content.contains(keyword))
            .or(festival.id.in(idListContainAllHashtags)))
        .orderBy(festivalTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .where(festivalTrans.title.contains(keyword)
            .or(festivalTrans.addressTag.contains(keyword))
            .or(festivalTrans.content.contains(keyword))
            .or(festival.id.in(idListContainAllHashtags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Locale locale) {
    return queryFactory
        .select(festival.id)
        .from(festival)
        .leftJoin(hashtag)
        .on(hashtag.postId.eq(festival.id)
            .and(hashtag.category.content.eq(CategoryContent.FESTIVAL))
            .and(hashtag.language.locale.eq(locale)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(festival.id)
        .having(festival.id.count().eq(splitKeyword(keyword).stream().count()))
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
