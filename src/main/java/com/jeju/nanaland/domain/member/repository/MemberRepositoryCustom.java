package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import java.util.List;
import com.jeju.nanaland.domain.member.entity.Member;

public interface MemberRepositoryCustom {

  MemberInfoDto findMemberWithLanguage(Long memberId);

  List<MemberConsent> findExpiredMemberConsent();

  List<Member> findInactiveMembersForWithdrawalDate();
}
