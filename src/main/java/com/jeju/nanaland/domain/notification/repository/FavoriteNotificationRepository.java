package com.jeju.nanaland.domain.notification.repository;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.notification.entity.FavoriteNotification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteNotificationRepository extends JpaRepository<FavoriteNotification, Long>,
    FavoriteNotificationRepositoryCustom {

  Optional<FavoriteNotification> findByMemberAndPostIdAndCategory(Member member, Long postId,
      Category category);
}
