package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;

public interface MemberRepositoryCustom {

  MemberInfoDto findMemberWithLanguage(Long memberId);
}
