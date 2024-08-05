package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.data.MemberNotificationCompose;
import com.jeju.nanaland.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {

  Page<Notification> findAllNotificationByMember(Member member, Pageable pageable);

  List<MemberNotificationCompose> findAllMemberNotificationCompose();
}
