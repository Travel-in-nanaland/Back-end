package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberFavoriteService {

  private final FavoriteRepository favoriteRepository;

  /**
   * 유저 찜목록에 있는 게시물 id 리스트 조회
   *
   * @param member 회원
   * @return 게시물 id 리스트
   */
  public List<Long> getFavoritePostIdsWithMember(Member member) {
    List<Favorite> favorites = favoriteRepository.findAllByMemberAndStatus(member, "ACTIVE");
    return favorites.stream().map(favorite -> favorite.getPost().getId()).toList();
  }

  /**
   * 게시물의 찜 여부 확인
   *
   * @param member   회원
   * @param category 게시물 카테고리
   * @param postId   게시물 id
   * @return boolean
   */
  public boolean isPostInFavorite(Member member, Category category, Long postId) {
    Optional<Favorite> favoriteOptional = favoriteRepository
        .findByMemberAndCategoryAndPostIdAndStatus(member, category, postId, "ACTIVE");

    return favoriteOptional.isPresent();
  }
}
