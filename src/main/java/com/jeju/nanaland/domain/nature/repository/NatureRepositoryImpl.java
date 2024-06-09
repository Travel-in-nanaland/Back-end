package com.jeju.nanaland.domain.nature.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.nature.entity.QNature.nature;
import static com.jeju.nanaland.domain.nature.entity.QNatureTrans.natureTrans;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.nature.dto.NatureCompositeDto;
import com.jeju.nanaland.domain.nature.dto.QNatureCompositeDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NatureRepositoryImpl implements NatureRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public NatureCompositeDto findCompositeDtoById(Long id, Locale locale) {
    return queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(nature.id.eq(id)
            .and(natureTrans.language.locale.eq(locale))
        )
        .fetchOne();
  }

  @Override
  public Page<NatureCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, locale);

    List<NatureCompositeDto> resultDto = queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            natureTrans.language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.locale.eq(locale))
        .where(natureTrans.title.contains(keyword)
            .or(natureTrans.addressTag.contains(keyword))
            .or(natureTrans.content.contains(keyword))
            .or(nature.id.in(idListContainAllHashtags)))
        .orderBy(natureTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.countDistinct())
        .from(nature)
        .leftJoin(nature.firstImageFile, imageFile)
        .leftJoin(nature.natureTrans, natureTrans)
        .on(natureTrans.language.locale.eq(locale))
        .where(natureTrans.title.contains(keyword)
            .or(natureTrans.addressTag.contains(keyword))
            .or(natureTrans.content.contains(keyword))
            .or(nature.id.in(idListContainAllHashtags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<NatureCompositeDto> findNatureThumbnails(Locale locale,
      List<String> addressFilterList, String keyword, Pageable pageable) {
    List<NatureCompositeDto> resultDto = queryFactory
        .select(new QNatureCompositeDto(
            nature.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            nature.contact,
            language.locale,
            natureTrans.title,
            natureTrans.content,
            natureTrans.address,
            natureTrans.addressTag,
            natureTrans.intro,
            natureTrans.details,
            natureTrans.time,
            natureTrans.amenity,
            natureTrans.fee
        ))
        .from(nature)
        .leftJoin(nature.natureTrans, natureTrans)
        .leftJoin(nature.firstImageFile, imageFile)
        .where(natureTrans.language.locale.eq(locale)
            .and(addressTagCondition(addressFilterList))
            .and(natureTrans.title.contains(keyword)))
        .orderBy(nature.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nature.count())
        .from(nature)
        .leftJoin(nature.natureTrans, natureTrans)
        .where(natureTrans.language.locale.eq(locale)
            .and(addressTagCondition(addressFilterList))
            .and(natureTrans.title.contains(keyword)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private BooleanExpression addressTagCondition(List<String> addressFilterList) {
    if (addressFilterList.isEmpty()) {
      return null;
    } else {
      return natureTrans.addressTag.in(addressFilterList);
    }
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Locale locale) {
    return queryFactory
        .select(nature.id)
        .from(nature)
        .leftJoin(hashtag)
        .on(hashtag.postId.eq(nature.id)
            .and(hashtag.category.content.eq(CategoryContent.NATURE))
            .and(hashtag.language.locale.eq(locale)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(nature.id)
        .having(nature.id.count().eq(splitKeyword(keyword).stream().count()))
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
