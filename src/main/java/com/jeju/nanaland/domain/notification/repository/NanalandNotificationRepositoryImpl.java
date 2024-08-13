package com.jeju.nanaland.domain.notification.repository;

import static com.jeju.nanaland.domain.notification.entity.QMemberNotification.memberNotification;
import static com.jeju.nanaland.domain.notification.entity.QNanalandNotification.nanalandNotification;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.QMember;
import com.jeju.nanaland.domain.notification.data.MemberNotificationCompose;
import com.jeju.nanaland.domain.notification.data.QMemberNotificationCompose;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class NanalandNotificationRepositoryImpl implements NanalandNotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<NanalandNotification> findAllNotificationByMember(Member member, Pageable pageable) {
    List<NanalandNotification> resultDto = queryFactory
        .selectFrom(nanalandNotification)
        .innerJoin(nanalandNotification.memberNotificationList, memberNotification)
        .innerJoin(QMember.member)
        .on(QMember.member.id.eq(memberNotification.memberId))
        .orderBy(nanalandNotification.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(nanalandNotification.count())
        .from(nanalandNotification)
        .innerJoin(nanalandNotification.memberNotificationList, memberNotification)
        .innerJoin(QMember.member)
        .on(QMember.member.id.eq(memberNotification.memberId));

    return PageableExecutionUtils.getPage(resultDto, pageable, countQuery::fetchOne);
  }

  @Override
  public List<MemberNotificationCompose> findAllMemberNotificationCompose() {
    return queryFactory
        .select(new QMemberNotificationCompose(
            memberNotification.id,
            memberNotification.memberId,
            nanalandNotification.notificationCategory,
            nanalandNotification.contentId
        ))
        .from(memberNotification)
        .innerJoin(memberNotification.nanalandNotification, nanalandNotification)
        .fetch();
  }

  @Override
  public Optional<NanalandNotification> findByNotificationInfo(
      NotificationCategory notificationCategory, Long contentId, String title, String content) {
    NanalandNotification result = queryFactory
        .selectFrom(nanalandNotification)
        .where(nanalandNotification.notificationCategory.eq(notificationCategory),
            nanalandNotification.contentId.eq(contentId),
            nanalandNotification.title.eq(title),
            nanalandNotification.content.eq(content)
        )
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
