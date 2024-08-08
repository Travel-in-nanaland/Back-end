package com.jeju.nanaland.domain.restaurant.repository;

import static com.jeju.nanaland.domain.common.entity.QImageFile.imageFile;
import static com.jeju.nanaland.domain.hashtag.entity.QHashtag.hashtag;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurant.restaurant;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantKeyword.restaurantKeyword;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantMenu.restaurantMenu;
import static com.jeju.nanaland.domain.restaurant.entity.QRestaurantTrans.restaurantTrans;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.QRestaurantResponse_RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantCompositeDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantMenuDto;
import com.jeju.nanaland.domain.restaurant.dto.RestaurantResponse.RestaurantThumbnail;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import com.jeju.nanaland.domain.review.dto.QReviewResponse_SearchPostForReviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.SearchPostForReviewDto;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
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
        .leftJoin(restaurantMenu.firstImageFile, imageFile)
        .where(restaurantMenu.restaurantTrans.restaurant.id.eq(postId)
            .and(restaurantTrans.language.eq(language)))
        .fetch();
  }

  @Override
  public Page<RestaurantCompositeDto> searchCompositeDtoByKeyword(String keyword, Language language,
      Pageable pageable) {
    List<Long> idListContainAllHashtags = getIdListContainAllHashtags(keyword, language);

    List<RestaurantCompositeDto> resultDto = queryFactory
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
        .leftJoin(restaurant.firstImageFile, imageFile)
        .leftJoin(restaurant.restaurantTrans, restaurantTrans)
        .on(restaurantTrans.language.eq(language))
        .where(restaurantTrans.title.contains(keyword)
            .or(restaurantTrans.addressTag.contains(keyword))
            .or(restaurantTrans.content.contains(keyword))
            .or(restaurant.id.in(idListContainAllHashtags)))
        .orderBy(restaurantTrans.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(restaurant.countDistinct())
        .from(restaurant)
        .leftJoin(restaurant.firstImageFile, imageFile)
        .leftJoin(restaurant.restaurantTrans, restaurantTrans)
        .on(restaurantTrans.language.eq(language))
        .where(restaurantTrans.title.contains(keyword)
            .or(restaurantTrans.addressTag.contains(keyword))
            .or(restaurantTrans.content.contains(keyword))
            .or(restaurant.id.in(idListContainAllHashtags)));

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

  private List<Long> getIdListContainAllHashtags(String keyword, Language language) {
    return queryFactory
        .select(restaurant.id)
        .from(restaurant)
        .leftJoin(hashtag)
        .on(hashtag.post.id.eq(restaurant.id)
            .and(hashtag.category.eq(Category.RESTAURANT))
            .and(hashtag.language.eq(language)))
        .where(hashtag.keyword.content.in(splitKeyword(keyword)))
        .groupBy(restaurant.id)
        .having(restaurant.id.count().eq(splitKeyword(keyword).stream().count()))
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
  public List<SearchPostForReviewDto> findAllSearchPostForReviewDtoByLanguage(Language language) {
    return queryFactory
        .select(
            new QReviewResponse_SearchPostForReviewDto(restaurant.id,
                Expressions.constant(Category.RESTAURANT.name()),
                restaurantTrans.title, restaurant.firstImageFile, restaurantTrans.address))
        .from(restaurant)
        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
        .where(restaurantTrans.language.eq(language))
        .fetch();
  }
}
