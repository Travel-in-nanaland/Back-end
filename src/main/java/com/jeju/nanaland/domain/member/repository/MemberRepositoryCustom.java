package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.MemberConsent;
import java.util.List;

public interface MemberRepositoryCustom {

  MemberInfoDto findMemberWithLanguage(Long memberId);

  List<MemberConsent> findExpiredMemberConsent();

}
