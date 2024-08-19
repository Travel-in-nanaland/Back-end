package com.jeju.nanaland.domain.notification.repository;

import static com.jeju.nanaland.domain.notification.entity.QFcmToken.fcmToken;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FcmTokenRepositoryImpl implements FcmTokenRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<FcmToken> findAllByMemberLanguage(Language language) {
    return queryFactory
        .selectFrom(fcmToken)
        .where(fcmToken.member.language.eq(language))
        .fetch();
  }

  @Override
  public List<FcmToken> findAllByMember(Member member) {
    return queryFactory
        .selectFrom(fcmToken)
        .where(fcmToken.member.eq(member))
        .fetch();
  }
}
