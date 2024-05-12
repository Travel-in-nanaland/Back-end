package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import java.util.Optional;

public interface MemberRepositoryCustom {

  Optional<Member> findDuplicateMember(String email, Provider provider,
      Long providerId);

  MemberInfoDto findMemberWithLanguage(Long memberId);
}
