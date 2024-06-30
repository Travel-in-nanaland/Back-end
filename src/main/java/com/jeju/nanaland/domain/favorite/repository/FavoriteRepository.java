package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>,
    FavoriteRepositoryCustom {

  Optional<Favorite> findByMemberAndCategoryAndPostId(Member member, Category category,
      Long postId);

  List<Favorite> findAllByMember(Member member);

  Page<Favorite> findAllByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);
}
