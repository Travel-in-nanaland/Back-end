package com.jeju.nanaland.domain.market.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.common.entity.QLanguage.language;
import static com.jeju.nanaland.domain.market.entity.QMarket.market;
import static com.jeju.nanaland.domain.market.entity.QMarketTrans.marketTrans;

import com.jeju.nanaland.domain.market.dto.MarketCompositeDto;
import com.jeju.nanaland.domain.market.dto.QMarketCompositeDto;
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
  public MarketCompositeDto findCompositeDtoById(Long id, String locale) {
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
  public Page<MarketCompositeDto> searchCompositeDtoByTitle(String title, String locale,
      Pageable pageable) {
    List<MarketCompositeDto> ResultDto = queryFactory
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
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(market.count())
        .from(market)
        .leftJoin(market.marketTrans, marketTrans)
        .where(marketTrans.title.contains(title)
            .and(marketTrans.language.locale.eq(locale)));

    return PageableExecutionUtils.getPage(ResultDto, pageable, countQuery::fetchOne);
  }
}
