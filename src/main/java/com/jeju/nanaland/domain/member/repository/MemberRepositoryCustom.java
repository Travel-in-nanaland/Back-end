package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import java.util.List;

public interface MemberRepositoryCustom {

  MemberInfoDto findMemberInfoDto(Long memberId);

  List<MemberConsent> findAllExpiredMemberConsent();

  List<Member> findAllInactiveMember();

  List<MemberConsent> findAllMemberConsent(Member member);
}
