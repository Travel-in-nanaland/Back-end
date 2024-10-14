package com.jeju.nanaland.domain.favorite.repository;

import static com.jeju.nanaland.domain.favorite.entity.QFavorite.favorite;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
@Slf4j
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Favorite> findAllFavoritesOrderByModifiedAtDesc(Member member, Pageable pageable) {
    List<Favorite> resultDto = queryFactory
        .selectFrom(favorite)
        .where(
            favorite.member.eq(member),
            favorite.status.eq("ACTIVE")
        )
        .orderBy(favorite.modifiedAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .where(
            favorite.member.eq(member),
            favorite.status.eq("ACTIVE")
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public Page<Favorite> findAllFavoritesOrderByModifiedAtDesc(Member member, Category category,
      Pageable pageable) {
    List<Favorite> resultDto = queryFactory
        .selectFrom(favorite)
        .where(
            favorite.member.eq(member),
            favorite.category.eq(category),
            favorite.status.eq("ACTIVE")
        )
        .orderBy(favorite.modifiedAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(favorite.count())
        .from(favorite)
        .where(
            favorite.member.eq(member),
            favorite.category.eq(category),
            favorite.status.eq("ACTIVE")
        );

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public List<Favorite> findAllFavoriteToSendNotification() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime threeMonthsAgo = now.minusMonths(3);
    LocalDateTime twoWeeksAgo = now.minusWeeks(2);

    return queryFactory
        .selectFrom(favorite)
        .where(
            favoriteNotification3MonthsCondition(threeMonthsAgo)
                .or(favoriteNotification2WeeksCondition(twoWeeksAgo))
        )
        .fetch();
  }

  private BooleanExpression favoriteNotification3MonthsCondition(LocalDateTime threeMonthsAgo) {
    return favorite.notificationCount.eq(1)
        .and(favorite.createdAt.before(threeMonthsAgo));
  }

  private BooleanExpression favoriteNotification2WeeksCondition(LocalDateTime twoWeeksAgo) {
    return favorite.notificationCount.eq(0)
        .and(favorite.createdAt.before(twoWeeksAgo));
  }
}
