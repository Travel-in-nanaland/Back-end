package com.jeju.nanaland.domain.member.repository;

import static com.jeju.nanaland.domain.member.entity.QMember.member;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<Member> findDuplicateMember(String email, Provider provider, Long providerId) {
    return queryFactory
        .selectFrom(member)
        .where(member.email.eq(email)
            .or(member.provider.eq(provider)
                .and(member.providerId.eq(providerId))
            ))
        .stream().findAny();
  }
}
