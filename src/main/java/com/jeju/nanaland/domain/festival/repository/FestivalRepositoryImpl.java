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
      LocalDate startDate, LocalDate endDate, int currentYear) {
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
        .where(seasonCondition(startDate, endDate, currentYear)
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
        // 축제의 시작은 다른 계절인데 축제 종료일이 계절의 시작 일을 넘기는 경우
        .where(seasonCondition(startDate, endDate, currentYear)
            .and(festivalTrans.language.locale.eq(locale)));

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

  private BooleanExpression seasonCondition(LocalDate startDate, LocalDate endDate,
      int currentYear) {
    LocalDate lastYearSeasonStartDate;

    if (startDate.getMonthValue() == 11) { // 겨울이면
      //작년 11월 1일
      lastYearSeasonStartDate = LocalDate.of(currentYear - 1, 11, 1);

      //올해 11월 1일
      LocalDate thisYearSeasonStartDate = LocalDate.of(currentYear, 11, 1);

      // 작년
      // 작년/올해 기준 축제 시작일이 11,12,1,2월이면
      return (festival.startDate.month().in(1, 2, 11, 12)
          .or(festival.endDate.month().in(1, 2, 11, 12))

          //작년에 11월 전에 시작해서 11월~에 포함 되는 경우
          .or(festival.startDate.loe(lastYearSeasonStartDate)
              .and(festival.endDate.goe(lastYearSeasonStartDate)))

          //올해 11월 전에 시작해서 11월~에 포함 되는 경우
          .or(festival.startDate.loe(thisYearSeasonStartDate)
              .and(festival.endDate.goe(thisYearSeasonStartDate))));

      /**
       *  23/8/20 ~ 24/4/2 포함
       *  23/8/30 <= 23/11/1 and 24/4/2 >= 23/11/1
       *
       *  23/11/2 ~ 23/12/16
       *  23/11/2 <= 23/11 and 23/12/16 >= 23/11
       *
       *  24/9/2 ~ 25/3/4
       *  24/9/2 <= 24/11/1 and 25/3/4>= 23/11/1
       *
       *  24/9/2 ~ 25/1/2
       *  24/9/2 <= 24/11/1 and 25/1/2>= 23/11/1
       *
       */

    } else { //겨울이 아니면
      return (festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
          // 축제의 기간이 필터 사이에 있을 대
          .or((festival.startDate.goe(startDate)
              .and(festival.endDate.loe(endDate))))
          // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
          .or((festival.startDate.loe(endDate)
              .and(festival.endDate.goe(endDate))));

      /**
       * 봄: 3,4 / 여름: 5,6,7,8 / 가을: 9, 10
       * 23/4/1 ~ 24/11/2
       *
       * 23/4/1<=24/10/31 and 24/11/2>= 24/10/31
       *       계절시작                       계절끝
       *   시작                                     끝
       */

    }
  }
}
//(festival.startDate.loe(startDate).and(festival.endDate.goe(startDate)))
//    // 축제의 기간이 필터 사이에 있을 대
//    .or((festival.startDate.goe(startDate)
//                    .and(festival.endDate.loe(endDate))))
//    // 축제의 시작일이 필터의 종료일 보다 작고 축제의 종료일은 필터의 종료일보다 클 때
//    .or((festival.startDate.loe(endDate))
//    .and(festival.endDate.goe(endDate))))
//
//    .and(festivalTrans.language.locale.eq(locale)
//                    .and(addressTagCondition(addressFilterList))

//YearMonth lastYearEndMonth = YearMonth.of(currentYear-1,4);
//
//lastYearStartDate = LocalDate.of(currentYear - 1, 3, 1);
//lastYearEndDate=LocalDate.of(currentYear-1,4,lastYearEndMonth.lengthOfMonth());