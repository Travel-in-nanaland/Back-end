package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.data.MemberNotificationCompose;
import com.jeju.nanaland.domain.notification.entity.NanalandNotification;
import com.jeju.nanaland.domain.notification.entity.eums.NotificationCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NanalandNotificationRepositoryCustom {

  Page<NanalandNotification> findAllNotificationByMember(Member member, Pageable pageable);

  List<MemberNotificationCompose> findAllMemberNotificationCompose();

  Optional<NanalandNotification> findByNotificationInfo(NotificationCategory notificationCategory,
      Long contentId, String title, String content);
}
