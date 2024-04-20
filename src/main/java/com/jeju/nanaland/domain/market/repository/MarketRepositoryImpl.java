package com.jeju.nanaland.domain.market.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;

import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.dto.QMarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.QMarketResponse_MarketThumbnail;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(market.id.eq(id).and(marketTrans.language.locale.eq(locale)))
        .fetchOne();
  }

  @Override
  public Page<MarketThumbnail> findMarketThumbnails(Locale locale, String addressFilter,
      Pageable pageable) {
    List<MarketThumbnail> resultDto = queryFactory
        .select(new QMarketResponse_MarketThumbnail(
            market.id,
            marketTrans.title,
            imageFile.thumbnailUrl,
            marketTrans.address
        ))
        .from(market)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.language.locale.eq(locale)
            .and(marketTrans.address.contains(addressFilter)))
        .orderBy(market.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.language.locale.eq(locale)
            .and(marketTrans.address.contains(addressFilter)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<MarketCompositeDto> searchCompositeDtoByTitle(String title, Locale locale,
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
            marketTrans.time,
            marketTrans.intro,
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.title.contains(title)
            .and(marketTrans.language.locale.eq(locale)))
        .orderBy(marketTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.title.contains(title)
            .and(marketTrans.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }
}
