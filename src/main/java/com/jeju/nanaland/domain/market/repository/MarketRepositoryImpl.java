package com.jeju.nanaland.domain.market.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;

import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.QMarketCompositeDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarketRepositoryImpl implements MarketRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public MarketCompositeDto findMarketCompositeDto(Long id, String locale) {
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
            marketTrans.amenity
        ))
        .from(market)
        .leftJoin(market.imageFile, imageFile)
        .leftJoin(market.marketTrans, marketTrans)
        .where(market.id.eq(id).and(marketTrans.language.locale.eq(locale)))
        .fetchOne();
  }
}
