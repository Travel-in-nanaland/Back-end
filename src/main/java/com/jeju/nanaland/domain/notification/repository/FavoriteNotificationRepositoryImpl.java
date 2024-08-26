package com.jeju.nanaland.domain.notification.repository;

import static com.jeju.nanaland.domain.notification.entity.QFavoriteNotification.favoriteNotification;

import com.jeju.nanaland.domain.notification.entity.FavoriteNotification;
import com.jeju.nanaland.domain.notification.entity.QFavoriteNotification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FavoriteNotificationRepositoryImpl implements FavoriteNotificationRepositoryCustom {

  private final JPQLQueryFactory queryFactory;

  @Override
  public List<FavoriteNotification> findAllFavoriteNotificationToSend() {

    return queryFactory
        .selectFrom(favoriteNotification)
        .where(
            favoriteNotification.isSent.isFalse(),
            favoriteNotification.status.eq("ACTIVE"),
            favoriteNotification.createdAt.before(LocalDateTime.now().minusMonths(1)),
            checkFavoriteMoreThan5()
        )
        .orderBy(favoriteNotification.createdAt.desc())
        .fetch();
  }

  private BooleanExpression checkFavoriteMoreThan5() {

    QFavoriteNotification sub = new QFavoriteNotification("fn2");

    return JPAExpressions.select(sub.count())
        .from(sub)
        .where(
            sub.category.eq(favoriteNotification.category),
            sub.postId.eq(favoriteNotification.postId),
            sub.createdAt.gt(favoriteNotification.createdAt)
        )
        .goe(5L);
  }
}
