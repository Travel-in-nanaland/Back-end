package com.jeju.nanaland.domain.market.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.QMarketCompositeDto;
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
public class MarketRepositoryImpl implements MarketRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public MarketCompositeDto findCompositeDtoById(Long id, Locale locale) {
    return queryFactory
        .select(new QMarketCompositeDto(
            market.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            market.contact,
            market.homepage,
            language.locale,
            marketTrans.title,
            marketTrans.content,
            marketTrans.address,
            marketTrans.addressTag,
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(market.id.eq(id).and(marketTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<MarketCompositeDto> findMarketThumbnails(Locale locale,
      List<String> addressFilterList,
      Pageable pageable) {
    List<MarketCompositeDto> resultDto = queryFactory
        .select(new QMarketCompositeDto(
            market.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            market.contact,
            market.homepage,
            language.locale,
            marketTrans.title,
            marketTrans.content,
            marketTrans.address,
            marketTrans.addressTag,
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.language.locale.eq(locale)
            .and(addressTagCondition(addressFilterList)))
        .orderBy(market.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.language.locale.eq(locale)
            .and(addressTagCondition(addressFilterList)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<MarketCompositeDto> searchCompositeDtoByKeyword(String keyword, Locale locale,
      Pageable pageable) {

    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, locale);

    List<MarketCompositeDto> resultDto = queryFactory
        .select(new QMarketCompositeDto(
            market.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            market.contact,
            market.homepage,
            marketTrans.language.locale,
            marketTrans.title,
            marketTrans.content,
            marketTrans.address,
            marketTrans.addressTag,
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.locale.eq(locale))
        .where(marketTrans.title.contains(keyword)
            .or(marketTrans.addressTag.contains(keyword))
            .or(marketTrans.content.contains(keyword))
            .or(market.id.in(idListContainAllHashtags)))
        .orderBy(marketTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .leftJoin(market.firstImageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .on(marketTrans.language.locale.eq(locale))
        .where(marketTrans.title.contains(keyword)
            .or(marketTrans.addressTag.contains(keyword))
            .or(marketTrans.content.contains(keyword))
            .or(market.id.in(idListContainAllHashtags)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private List<Long> getIdListContainAllHashtags(String keyword, Locale locale) {
    return queryFactory
        .select(market.id)
        .from(market)
        .leftJoin(hashtag)
        .on(hashtag.postId.eq(market.id)
            .and(hashtag.category.content.eq(CategoryContent.MARKET))
            .and(hashtag.language.locale.eq(locale)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(market.id)
        .having(market.id.count().eq(splitKeyword(keyword).stream().count()))
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

  private BooleanExpression addressTagCondition(List<String> addressFilterList) {
    if (addressFilterList.isEmpty()) {
      return null;
    } else {
      return marketTrans.addressTag.in(addressFilterList);
    }
  }
}
