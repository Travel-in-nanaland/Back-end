package com.jeju.nanaland.domain.festival.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.festival.entity.QFestival.festival;
import static com.jeju.nanaland.domain.festival.entity.QFestivalTrans.festivalTrans;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;

import com.jeju.nanaland.domain.common.data.AddressTag;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.data.Status;
import com.jeju.nanaland.domain.common.dto.PostPreviewDto;
import com.jeju.nanaland.domain.common.dto.QPostPreviewDto;
import com.jeju.nanaland.domain.festival.dto.FestivalCompositeDto;
import com.jeju.nanaland.domain.festival.dto.QFestivalCompositeDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
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
  public FestivalCompositeDto findCompositeDtoById(Long id, Language language) {
    return queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language,
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
        .where(festival.id.eq(id).and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
        )
        .fetchOne();
  }

  @Override
  public FestivalCompositeDto findCompositeDtoByIdWithPessimisticLock(Long id, Language language) {
    return queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language,
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
        .where(festival.id.eq(id).and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
        )
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .fetchOne();
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByKeyword(String keyword, Language language,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, language);

    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language,
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
        .on(festivalTrans.language.eq(language))
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
        .on(festivalTrans.language.eq(language))
        .where(festivalTrans.title.contains(keyword)
            .or(festivalTrans.addressTag.contains(keyword))
            .or(festivalTrans.content.contains(keyword))
            .or(festival.id.in(idListContainAllHashtags))
            .and(festival.status.eq(Status.ACTIVE)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(festival.id)
        .from(festival)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(festival.id)
            .and(hashtag.category.eq(Category.FESTIVAL))
            .and(hashtag.language.eq(language)))
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
  public Page<FestivalCompositeDto> searchCompositeDtoByOnGoing(Language language,
      Pageable pageable, boolean onGoing, List<AddressTag> addressTags) {
    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language,
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
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
            .and(festivalTrans.language.eq(language))
            .and(addressTagCondition(language, addressTags))
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
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
            .and(addressTagCondition(language, addressTags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoBySeason(Language language, Pageable pageable,
      String season) {
    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language,
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
            .and(festivalTrans.language.eq(language))
            .and(festivalTrans.language.eq(language))
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
            .and(festivalTrans.language.eq(language))
            .and(festival.status.eq(Status.ACTIVE))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<FestivalCompositeDto> searchCompositeDtoByMonth(Language language, Pageable pageable,
      LocalDate startDate, LocalDate endDate, List<AddressTag> addressTags) {
    List<FestivalCompositeDto> resultDto = queryFactory
        .select(new QFestivalCompositeDto(
            festival.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            festival.contact,
            festival.homepage,
            festivalTrans.language,
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

                .and(festivalTrans.language.eq(language)
                    .and(addressTagCondition(language, addressTags)))
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

                .and(festivalTrans.language.eq(language)
                    .and(addressTagCondition(language, addressTags)))

                .and(festival.status.eq(Status.ACTIVE))
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public PostPreviewDto findPostPreviewDto(Long postId, Language language) {
    return queryFactory
        .select(new QPostPreviewDto(
            festival.id,
            festivalTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(festival)
        .innerJoin(festival.festivalTrans, festivalTrans)
        .innerJoin(festival.firstImageFile, imageFile)
        .where(
            festival.id.eq(postId),
            festivalTrans.language.eq(language))
        .fetchOne();
  }

  private BooleanExpression addressTagCondition(Language language, List<AddressTag> addressTags) {
    if (addressTags.isEmpty()) {
      return null;
    } else {
      List<String> addressTagFilters = addressTags.stream()
          .map(address -> address.getValueByLocale(language)).toList();
      return festivalTrans.addressTag.in(addressTagFilters);
    }
  }
}
