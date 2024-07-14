package com.jeju.nanaland.domain.restaurant.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurant.restaurant;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantKeyword.restaurantKeyword;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantTrans.restaurantTrans;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<RestaurantThumbnail> findRestaurantThumbnails(Language language,
      List<RestaurantTypeKeyword> keywordFilter, List<String> addressFilter, Pageable pageable) {
    List<RestaurantThumbnail> resultDto = queryFactory
        .selectDistinct(new QRestaurantResponse_RestaurantThumbnail(
            restaurant.id,
            restaurantTrans.title,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            restaurantTrans.addressTag
        ))
        .from(restaurant)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurantKeyword)
        .on(restaurantKeyword.restaurant.eq(restaurant))
        .where(restaurantTrans.language.eq(language)
            .and(keywordCondition(keywordFilter))
            .and(addressTagCondition(addressFilter)))
        .orderBy(restaurant.priority.desc(),
            restaurant.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .selectDistinct(restaurant.count())
        .from(restaurant)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurantKeyword)
        .on(restaurantKeyword.restaurant.eq(restaurant))
        .where(restaurantTrans.language.eq(language)
            .and(keywordCondition(keywordFilter))
            .and(addressTagCondition(addressFilter)));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  private BooleanExpression addressTagCondition(List<String> addressFilterList) {
    if (addressFilterList.isEmpty()) {
      return null;
    } else {
      return restaurantTrans.addressTag.in(addressFilterList);
    }
  }

  private BooleanExpression keywordCondition(List<RestaurantTypeKeyword> keywordFilterList) {
    if (keywordFilterList.isEmpty()) {
      return null;
    } else {
      return restaurantKeyword.restaurantTypeKeyword.in(keywordFilterList);
    }
  }
}
