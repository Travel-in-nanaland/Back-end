package com.jeju.nanaland.domain.festival.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalCompositeDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
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
            festival.endDate
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.id.eq(id).and(festivalTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
      Pageable pageable) {

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
            festival.endDate
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festivalTrans.title.contains(title)
            .and(festivalTrans.language.locale.eq(locale)))
        .orderBy(festivalTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festivalTrans.title.contains(title)
            .and(festivalTrans.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Locale locale, Pageable pageable,
      boolean onGoing) {
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
            festival.endDate
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.onGoing.eq(onGoing)
            .and(festivalTrans.language.locale.eq(locale)))
        .orderBy(festivalTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        .where(festival.onGoing.eq(onGoing)
            .and(festivalTrans.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoBySeason(Locale locale, Pageable pageable,
      LocalDate startDate, LocalDate endDate) {
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
            festival.endDate
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        // 축제의 시작은 다른 계절인데 축제 종료일이 계절의 시작 일을 넘기는 경우
        .where((festival.startDate.month().loe(startDate.getMonthValue())
            .and(festival.endDate.month().goe(startDate.getMonthValue())))
            // 축제의 시작과 끝이 계절안에 포함인 경우
            .or(festival.startDate.month().goe(startDate.getMonthValue())
                .and(festival.endDate.month().loe(endDate.getMonthValue())))
            // 축제의 시작은 계절에 포함되는데 축제의 끝은 다른 계절인 경우
            .or(festival.startDate.month().loe(endDate.getMonthValue())
                .and(festival.endDate.month().goe(endDate.getMonthValue()))))
        .orderBy(festivalTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
        .leftJoin(festival.festivalTrans, festivalTrans)
        // 축제의 시작은 다른 계절인데 축제 종료일이 계절의 시작 일을 넘기는 경우
        .where((festival.startDate.month().loe(startDate.getMonthValue())
            .and(festival.endDate.month().goe(startDate.getMonthValue())))
            // 축제의 시작과 끝이 계절안에 포함인 경우
            .or(festival.startDate.month().goe(startDate.getMonthValue())
                .and(festival.endDate.month().loe(endDate.getMonthValue())))
            // 축제의 시작은 계절에 포함되는데 축제의 끝은 다른 계절인 경우
            .or(festival.startDate.month().loe(endDate.getMonthValue())
                .and(festival.endDate.month().goe(endDate.getMonthValue())))
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
            festival.endDate
        ))
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
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
        )
        .orderBy(festivalTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(festival.count())
        .from(festival)
        .leftJoin(festival.imageFile, imageFile)
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
