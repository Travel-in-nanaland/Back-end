package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  Optional<Favorite> findByMemberAndCategoryAndPostId(Member member, Category category,
      Long postId);

  List<Favorite> findAllByMemberAndCategory(Member member, Category category);
}
