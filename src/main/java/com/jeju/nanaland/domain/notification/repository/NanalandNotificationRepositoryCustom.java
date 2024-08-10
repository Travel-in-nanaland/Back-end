package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.data.MemberNotificationCompose;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NanalandNotificationRepositoryCustom {

  Page<NanalandNotification> findAllNotificationByMember(Member member, Pageable pageable);

  List<MemberNotificationCompose> findAllMemberNotificationCompose();
}
