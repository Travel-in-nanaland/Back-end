package com.jeju.nanaland.domain.member.repository;

import static com.jeju.nanaland.domain.member.entity.QMember.member;

import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.QMemberResponse_MemberInfoDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public MemberInfoDto findMemberWithLanguage(Long memberId) {

    return queryFactory
        .select(new QMemberResponse_MemberInfoDto(
            member, member.language
        ))
        .from(member)
        .leftJoin(member.language)
        .where(member.id.eq(memberId).and(member.status.eq(Status.ACTIVE)))
        .fetchOne();
  }
}
