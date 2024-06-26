package com.jeju.nanaland.domain.member.repository;

import static com.jeju.nanaland.domain.member.entity.QMember.member;
import static com.jeju.nanaland.domain.member.entity.QMemberConsent.memberConsent;
import static com.jeju.nanaland.domain.member.entity.QMemberWithdrawal.memberWithdrawal;

import com.jeju.nanaland.domain.common.entity.Status;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.dto.QMemberResponse_MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import com.jeju.nanaland.domain.member.entity.enums.ConsentType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
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

  @Override
  public List<Member> findInactiveMembersForWithdrawalDate() {
    LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

    return queryFactory
        .select(member)
        .from(member)
        .leftJoin(memberWithdrawal)
        .on(memberWithdrawal.member.eq(member))
        .where(member.status.eq(Status.INACTIVE)
            .and(member.providerId.ne("INACTIVE"))
            .and(memberWithdrawal.withdrawalDate.before(threeMonthsAgo.atStartOfDay())))
        .fetch();
  }

  @Override
  public List<MemberConsent> findMemberConsentByMember(Member member) {
    return queryFactory
        .selectFrom(memberConsent)
        .where(memberConsent.consentType.ne(ConsentType.TERMS_OF_USE)
            .and(memberConsent.member.eq(member)))
        .fetch();
  }

  @Override
  public List<MemberConsent> findExpiredMemberConsent() {
    LocalDate expirationDate = LocalDate.now().minusYears(1).minusMonths(6);

    return queryFactory
        .selectFrom(memberConsent)
        .where(memberConsent.consent.eq(true)
            .and(memberConsent.consentDate.before(expirationDate.atStartOfDay())))
        .fetch();
  }
}
