package com.jeju.nanaland.domain.favorite.repository;

import static com.jeju.nanaland.domain.favorite.entity.QFavorite.favorite;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.Thumbnail;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Thumbnail> findNatureThumbnails(Member member, Language language,
      Pageable pageable) {
//    List<Thumbnail> resultDto = queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            nature.id,
//            natureTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(nature)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(nature.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.NATURE))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(nature.natureTrans, natureTrans)
//        .on(natureTrans.language.eq(language))
//        .innerJoin(nature.firstImageFile, imageFile)
//        .orderBy(favorite.createdAt.desc())
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//        .fetch();
//
//    JPAQuery<Long> countQuery = queryFactory
//        .select(nature.count())
//        .from(nature)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(nature.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.NATURE))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(nature.natureTrans, natureTrans)
//        .on(natureTrans.language.eq(language))
//        .innerJoin(nature.firstImageFile, imageFile);
//
//    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
    return null;
  }

  @Override
  public Thumbnail findNatureThumbnailByPostId(Member member, Long postId, Language language) {
//    return queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            nature.id,
//            natureTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(nature)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(nature.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.NATURE))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(nature.natureTrans, natureTrans)
//        .on(natureTrans.language.eq(language))
//        .innerJoin(nature.firstImageFile, imageFile)
//        .where(favorite.post.id.eq(postId))
//        .fetchOne();
    return null;

  }

  @Override
  public Page<Thumbnail> findExperienceThumbnails(Member member, Language language,
      Pageable pageable) {
    return null;

//    List<ThumbnailDto> resultDto = queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            experience.id,
//            experienceTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(experience)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(experience.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.EXPERIENCE))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(experience.experienceTrans, experienceTrans)
//        .on(experienceTrans.language.eq(language))
//        .innerJoin(experience.firstImageFile, imageFile)
//        .orderBy(favorite.createdAt.desc())
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//        .fetch();
//
//    JPAQuery<Long> countQuery = queryFactory
//        .select(experience.count())
//        .from(experience)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(experience.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.EXPERIENCE))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(experience.experienceTrans, experienceTrans)
//        .on(experienceTrans.language.eq(language))
//        .innerJoin(experience.firstImageFile, imageFile);
//
//    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Thumbnail findExperienceThumbnailByPostId(Member member, Long postId,
      Language language) {
    return null;

//    return queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            experience.id,
//            experienceTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(experience)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(experience.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.EXPERIENCE))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(experience.experienceTrans, experienceTrans)
//        .on(experienceTrans.language.eq(language))
//        .innerJoin(experience.firstImageFile, imageFile)
//        .where(favorite.post.id.eq(postId))
//        .fetchOne();
  }

  @Override
  public Page<Thumbnail> findFestivalThumbnails(Member member, Language language,
      Pageable pageable) {
    return null;

//    List<ThumbnailDto> resultDto = queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            festival.id,
//            festivalTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(festival)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(festival.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.FESTIVAL))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(festival.festivalTrans, festivalTrans)
//        .on(festivalTrans.language.eq(language))
//        .innerJoin(festival.firstImageFile, imageFile)
//        .orderBy(favorite.createdAt.desc())
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//        .fetch();
//
//    JPAQuery<Long> countQuery = queryFactory
//        .select(festival.count())
//        .from(festival)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(festival.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.FESTIVAL))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(festival.festivalTrans, festivalTrans)
//        .on(festivalTrans.language.eq(language))
//        .innerJoin(festival.firstImageFile, imageFile);
//
//    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Thumbnail findFestivalThumbnailByPostId(Member member, Long postId, Language language) {
    return null;

//    return queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            festival.id,
//            festivalTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(festival)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(festival.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.FESTIVAL))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(festival.festivalTrans, festivalTrans)
//        .on(festivalTrans.language.eq(language))
//        .innerJoin(festival.firstImageFile, imageFile)
//        .where(favorite.post.id.eq(postId))
//        .fetchOne();
  }

  @Override
  public Page<Thumbnail> findMarketThumbnails(Member member, Language language,
      Pageable pageable) {
    return null;

//    List<ThumbnailDto> resultDto = queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            market.id,
//            marketTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(market)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(market.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.MARKET))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(market.marketTrans, marketTrans)
//        .on(marketTrans.language.eq(language))
//        .innerJoin(market.firstImageFile, imageFile)
//        .orderBy(favorite.createdAt.desc())
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//        .fetch();
//
//    JPAQuery<Long> countQuery = queryFactory
//        .select(market.count())
//        .from(market)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(market.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.MARKET))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(market.marketTrans, marketTrans)
//        .on(marketTrans.language.eq(language))
//        .innerJoin(market.firstImageFile, imageFile);
//
//    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Thumbnail findMarketThumbnailByPostId(Member member, Long postId, Language language) {
    return null;

//    return queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            market.id,
//            marketTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(market)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(market.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.MARKET))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(market.marketTrans, marketTrans)
//        .on(marketTrans.language.eq(language))
//        .innerJoin(market.firstImageFile, imageFile)
//        .where(favorite.post.id.eq(postId))
//        .fetchOne();
  }

  @Override
  public Page<Thumbnail> findNanaThumbnails(Member member, Language language,
      Pageable pageable) {
    return null;

//    List<ThumbnailDto> resultDto = queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            nana.id,
//            nanaTitle.heading,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(nana)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(nana.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.NANA))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(nanaTitle)
//        .on(nanaTitle.nana.eq(nana)
//            .and(nanaTitle.language.eq(language)))
//        .innerJoin(nana.firstImageFile, imageFile)
//        .orderBy(favorite.createdAt.desc())
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//        .fetch();
//
//    JPAQuery<Long> countQuery = queryFactory
//        .select(nana.count())
//        .from(nana)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(nana.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.NANA))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(nanaTitle)
//        .on(nanaTitle.nana.eq(nana).and(nanaTitle.language.eq(language)))
//        .innerJoin(nana.firstImageFile, imageFile);
//
//    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Thumbnail findNanaThumbnailByPostId(Member member, Long postId, Language language) {
    return null;

//    return queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            nana.id,
//            nanaTitle.heading,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(nana)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(nana.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.NANA))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(nanaTitle)
//        .on(nana.eq(nanaTitle.nana)
//            .and(nanaTitle.language.eq(language)))
//        .innerJoin(nana.firstImageFile, imageFile)
//        .where(favorite.post.id.eq(postId))
//        .fetchOne();
  }

  @Override
  public Page<Thumbnail> findRestaurantThumbnails(Member member, Language language,
      Pageable pageable) {
    return null;

//    List<ThumbnailDto> resultDto = queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            restaurant.id,
//            restaurantTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(restaurant)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(restaurant.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.RESTAURANT))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
//        .on(restaurantTrans.language.eq(language))
//        .innerJoin(restaurant.firstImageFile, imageFile)
//        .orderBy(favorite.createdAt.desc())
//        .offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//        .fetch();
//
//    JPAQuery<Long> countQuery = queryFactory
//        .select(restaurant.count())
//        .from(restaurant)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(restaurant.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.RESTAURANT))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
//        .on(restaurantTrans.language.eq(language))
//        .innerJoin(restaurant.firstImageFile, imageFile);
//
//    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Thumbnail findRestaurantThumbnailByPostId(Member member, Long postId,
      Language language) {
    return null;
//    return queryFactory
//        .select(new QFavoriteResponse_ThumbnailDto(
//            restaurant.id,
//            restaurantTrans.title,
//            favorite.category.stringValue(),
//            imageFile.originUrl,
//            imageFile.thumbnailUrl
//        ))
//        .from(restaurant)
//        .innerJoin(favorite)
//        .on(favorite.post.id.eq(restaurant.id)
//            .and(favorite.member.eq(member))
//            .and(favorite.category.eq(Category.RESTAURANT))
//            .and(favorite.status.eq("ACTIVE")))
//        .innerJoin(restaurant.restaurantTrans, restaurantTrans)
//        .on(restaurantTrans.language.eq(language))
//        .innerJoin(restaurant.firstImageFile, imageFile)
//        .where(favorite.post.id.eq(postId))
//        .fetchOne();
  }

  @Override
  public List<Favorite> findAllFavoriteToSendNotification() {
    return queryFactory
        .selectFrom(favorite)
        .where(favoriteNotificationAfter3MonthsCondition().or(
            favoriteNotificationAfter2WeeksCondition()))
        .fetch();
  }

  private BooleanExpression favoriteNotificationAfter3MonthsCondition() {
    return favorite.notificationCount.eq(1)
        .and(favorite.createdAt.before(LocalDateTime.now().minusMonths(3)));
  }

  private BooleanExpression favoriteNotificationAfter2WeeksCondition() {
    return favorite.notificationCount.eq(0)
        .and(favorite.createdAt.before(LocalDateTime.now().minusWeeks(2)));
  }
}
