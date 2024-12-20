package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

  Page<Favorite> findAllFavoritesOrderByModifiedAtDesc(Member member, Pageable pageable);

  Page<Favorite> findAllFavoritesOrderByModifiedAtDesc(Member member, Category category,
      Pageable pageable);

  Page<Favorite> findAllExperienceFavoritesOrderByModifiedAtDesc(Member member,
      ExperienceType experienceType, Pageable pageable);

  List<Favorite> findAllFavoriteToSendNotification();
}
