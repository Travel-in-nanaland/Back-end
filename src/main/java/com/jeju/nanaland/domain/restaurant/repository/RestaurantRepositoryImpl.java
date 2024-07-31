package com.jeju.nanaland.domain.restaurant.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurant.restaurant;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantKeyword.restaurantKeyword;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantMenu.restaurantMenu;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantTrans.restaurantTrans;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  @Override
  public RestaurantCompositeDto findCompositeDtoById(Long postId, Language language) {
    return queryFactory
        .select(new QRestaurantCompositeDto(
            restaurant.id,
            imageFile.originUrl,
            imageFile.thumbnailUrl,
            restaurant.contact,
            restaurantTrans.language,
            restaurantTrans.title,
            restaurantTrans.content,
            restaurantTrans.address,
            restaurantTrans.addressTag,
            restaurantTrans.time,
            restaurant.homepage,
            restaurant.instagram,
            restaurantTrans.service
        ))
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .innerJoin(restaurant.firstImageFile, imageFile)
        .where(restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .fetchOne();
  }

  @Override
  public Set<RestaurantTypeKeyword> getRestaurantTypeKeywordSet(Long postId) {
    Map<Long, Set<RestaurantTypeKeyword>> map = queryFactory
        .selectFrom(restaurantKeyword)
        .where(restaurantKeyword.restaurant.id.eq(postId))
        .transform(GroupBy.groupBy(restaurantKeyword.restaurant.id)
            .as(GroupBy.set(restaurantKeyword.restaurantTypeKeyword)));

    return map.getOrDefault(postId, Collections.emptySet());
  }

  @Override
  public List<RestaurantMenuDto> getRestaurantMenuList(Long postId, Language language) {
    return queryFactory
        .select(new QRestaurantResponse_RestaurantMenuDto(
            restaurantMenu.menuName,
            restaurantMenu.price,
            imageFile.originUrl,
            imageFile.thumbnailUrl
        ))
        .from(restaurantMenu)
        .innerJoin(restaurantMenu.firstImageFile, imageFile)
        .where(restaurantMenu.restaurantTrans.restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .fetch();
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
