package com.jeju.nanaland.domain.festival.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalCompositeDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
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
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.id.eq(id).and(festivalTrans.language.locale.eq(locale))
            .and(festival.status.eq(Status.ACTIVE))
        )
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
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .where(festivalTrans.title.contains(keyword)
            .or(festivalTrans.addressTag.contains(keyword))
            .or(festivalTrans.content.contains(keyword))
            .or(festival.id.in(idListContainAllHashtags))
            .and(festival.status.eq(Status.ACTIVE)))
        .orderBy(festivalTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .on(festivalTrans.language.locale.eq(locale))
        .where(festivalTrans.title.contains(keyword)
            .or(festivalTrans.addressTag.contains(keyword))
            .or(festivalTrans.content.contains(keyword))
            .or(festival.id.in(idListContainAllHashtags))
            .and(festival.status.eq(Status.ACTIVE)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Locale locale) {
    return queryFactory
        .select(festival.id)
        .from(festival)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(festival.id)
            .and(hashtag.category.eq(Category.FESTIVAL))
            .and(hashtag.language.locale.eq(locale)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword))
            .and(festival.status.eq(Status.ACTIVE)))
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

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Locale locale, Pageable pageable,
      boolean onGoing, List<String> addressFilterList) {
    List<FestivalCompositeDto> resultDto = queryFactory
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
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.onGoing.eq(onGoing)
            .and(festivalTrans.language.locale.eq(locale))
            .and(festival.status.eq(Status.ACTIVE))
            .and(festivalTrans.language.locale.eq(locale))
            .and(addressTagCondition(addressFilterList))
        )
        .orderBy(festival.endDate.desc()) // 최근에 끝난 순
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.onGoing.eq(onGoing)
            .and(festivalTrans.language.locale.eq(locale))
            .and(festival.status.eq(Status.ACTIVE))
            .and(addressTagCondition(addressFilterList)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoBySeason(Locale locale, Pageable pageable,
      String season) {
    List<FestivalCompositeDto> resultDto = queryFactory
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
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.season.like("%" + season + "%")
            .and(festivalTrans.language.locale.eq(locale))
            .and(festivalTrans.language.locale.eq(locale))
            .and(festival.status.eq(Status.ACTIVE)))
        .orderBy(festival.endDate.asc())// 종료일 오름차 순 (곧 종료되는)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.season.like("%" + season + "%")
            .and(festivalTrans.language.locale.eq(locale))
            .and(festival.status.eq(Status.ACTIVE))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByMonth(Locale locale, Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<String> addressFilterList) {
    List<FestivalCompositeDto> resultDto = queryFactory
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
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where( // 축제의 시작일이 필터 시작일 보다 작고 축제의 종료일은 필터의 시작 일보다 클 때
            ((festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
                // 축제의 기간이 필터 사이에 있을 대
                .or((festival.startDate.goe(startDate)
                    .and(festival.endDate.loe(endDate))))
                // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
                .or((festival.startDate.loe(endDate))
                    .and(festival.endDate.goe(endDate))))

                .and(festivalTrans.language.locale.eq(locale)
                    .and(addressTagCondition(addressFilterList)))
                .and(festival.status.eq(Status.ACTIVE))
        )
        .orderBy(festival.endDate.asc()) // 종료일 오름차 순 (곧 종료되는)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.firstImageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where( // 축제의 시작일이 필터 시작일 보다 작고 축제의 종료일은 필터의 시작 일보다 클 때
            ((festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
                // 축제의 기간이 필터 사이에 있을 대
                .or((festival.startDate.goe(startDate)
                    .and(festival.endDate.loe(endDate))))
                // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
                .or((festival.startDate.loe(endDate))
                    .and(festival.endDate.goe(endDate))))

                .and(festivalTrans.language.locale.eq(locale)
                    .and(addressTagCondition(addressFilterList)))

                .and(festival.status.eq(Status.ACTIVE))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private BooleanExpression addressTagCondition(List<String> addressFilterList) {
    if (addressFilterList.isEmpty()) {
      return null;
    } else {
      return festivalTrans.addressTag.in(addressFilterList);
    }
  }
}
