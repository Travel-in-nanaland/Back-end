package com.jeju.nanaland.domain.member.repository;

import static com.jeju.nanaland.domain.member.entity.QMember.member;
import static com.jeju.nanaland.domain.member.entity.QMemberConsent.memberConsent;
import static com.jeju.nanaland.domain.member.entity.QMemberWithdrawal.memberWithdrawal;

import com.jeju.nanaland.domain.common.data.Status;
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

  /**
   * 회원 정보 조회
   * 
   * @param memberId 회원 ID
   * @return 회원 정보
   */
  @Override
  public MemberInfoDto findMemberInfoDto(Long memberId) {

    return queryFactory
        .select(new QMemberResponse_MemberInfoDto(
            member, member.language
        ))
        .from(member)
        .where(member.id.eq(memberId).and(member.status.eq(Status.ACTIVE)))
        .fetchOne();
  }

  /**
   * 동의일이 1년 6개월이 지나 만료된 이용약관 조회
   * 
   * @return 만료된 이용약관 리스트
   */
  @Override
  public List<MemberConsent> findAllExpiredMemberConsent() {
    LocalDate expirationDate = LocalDate.now().minusYears(1).minusMonths(6);

    return queryFactory
        .selectFrom(memberConsent)
        .where(memberConsent.consent.eq(true)
            .and(memberConsent.consentDate.before(expirationDate.atStartOfDay())))
        .fetch();
  }

  /**
   * 비활성화 후 3개월이 지난 회원 조회
   * 
   * @return 비활성화 회원 리스트
   */
  @Override
  public List<Member> findAllInactiveMember() {
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

  /**
   * 회원의 이용약관 조회
   * 필수 이용약관은 제외
   * @param member 회원
   * @return 이용약관 리스트
   */
  @Override
  public List<MemberConsent> findAllMemberConsent(Member member) {
    return queryFactory
        .selectFrom(memberConsent)
        .where(memberConsent.consentType.ne(ConsentType.TERMS_OF_USE)
            .and(memberConsent.member.eq(member)))
        .fetch();
  }
}
