package com.jeju.nanaland.domain.member.repository;

import static com.jeju.nanaland.domain.member.entity.QMember.member;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.QMemberResponse_MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
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

  @Override
  public MemberInfoDto findMemberWithLanguage(Long memberId) {

    return queryFactory
        .select(new QMemberResponse_MemberInfoDto(
            member, member.language
        ))
        .from(member)
        .leftJoin(member.language)
        .where(member.id.eq(memberId))
        .fetchOne();
  }
}
