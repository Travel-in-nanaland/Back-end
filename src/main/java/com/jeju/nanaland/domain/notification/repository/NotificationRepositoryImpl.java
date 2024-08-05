package com.jeju.nanaland.domain.notification.repository;

import static com.jeju.nanaland.domain.notification.entity.QMemberNotification.memberNotification;
import static com.jeju.nanaland.domain.notification.entity.QNotification.notification;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.QMember;
import com.jeju.nanaland.domain.notification.entity.Notification;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Notification> findAllNotificationByMember(Member member, Pageable pageable) {
    List<Notification> resultDto = queryFactory
        .selectFrom(notification)
        .innerJoin(memberNotification.notification, notification)
        .innerJoin(QMember.member)
        .on(QMember.member.id.eq(memberNotification.memberId))
        .orderBy(notification.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(notification.count())
        .from(notification)
        .innerJoin(memberNotification.notification, notification)
        .innerJoin(QMember.member)
        .on(QMember.member.id.eq(memberNotification.memberId));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }
}
