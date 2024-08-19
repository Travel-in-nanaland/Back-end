package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.entity.FcmToken;
import java.util.List;

public interface FcmTokenRepositoryCustom {

  List<FcmToken> findAllByMemberLanguage(Language language);

  List<FcmToken> findAllByMember(Member member);
}
