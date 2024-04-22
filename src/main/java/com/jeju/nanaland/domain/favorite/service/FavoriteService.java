package com.jeju.nanaland.domain.favorite.service;

import static com.jeju.nanaland.domain.common.data.CategoryContent.EXPERIENCE;
import static com.jeju.nanaland.domain.common.data.CategoryContent.FESTIVAL;
import static com.jeju.nanaland.domain.common.data.CategoryContent.MARKET;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NANA;
import static com.jeju.nanaland.domain.common.data.CategoryContent.NATURE;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.repository.CategoryRepository;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.global.exception.ServerErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

  private final CategoryRepository categoryRepository;
  private final FavoriteRepository favoriteRepository;

  public List<Long> getMemberFavoritePostIds(Member member, CategoryContent categoryContent) {

    Category category = getCategoryFromCategoryContent(categoryContent);
    List<Favorite> favorites = favoriteRepository.findAllByMemberAndCategory(member, category);

    List<Long> postIds = new ArrayList<>();
    for (Favorite favorite : favorites) {
      postIds.add(favorite.getPostId());
    }
    return postIds;
  }

  @Transactional
  public String toggleLikeStatus(Member member, CategoryContent categoryContent, Long postId) {

    Category category = getCategoryFromCategoryContent(categoryContent);

    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        member, category, postId);

    // 좋아요 상태일 때
    if (favoriteOptional.isPresent()) {
      Favorite favorite = favoriteOptional.get();

      // 좋아요 삭제
      favoriteRepository.delete(favorite);
      return "좋아요 삭제";
    }
    // 좋아요 상태가 아닐 때
    else {
      Favorite favorite = Favorite.builder()
          .member(member)
          .category(category)
          .postId(postId)
          .build();

      // 좋아요 추가
      favoriteRepository.save(favorite);
      return "좋아요 추가";
    }
  }

  private Category getCategoryFromCategoryContent(CategoryContent categoryContent) {
    return switch (categoryContent) {
      case NANA -> categoryRepository.findByContent(NANA)
          .orElseThrow(() -> new ServerErrorException("NANA에 해당하는 카테고리가 없습니다."));

      case EXPERIENCE -> categoryRepository.findByContent(EXPERIENCE)
          .orElseThrow(() -> new ServerErrorException("EXPERIENCE에 해당하는 카테고리가 없습니다."));

      case NATURE -> categoryRepository.findByContent(NATURE)
          .orElseThrow(() -> new ServerErrorException("NATURE에 해당하는 카테고리가 없습니다."));

      case MARKET -> categoryRepository.findByContent(MARKET)
          .orElseThrow(() -> new ServerErrorException("MARKET에 해당하는 카테고리가 없습니다."));

      case FESTIVAL -> categoryRepository.findByContent(FESTIVAL)
          .orElseThrow(() -> new ServerErrorException("FESTIVAL에 해당하는 카테고리가 없습니다."));
    };
  }

  public boolean isPostInFavorite(Member member, CategoryContent categoryContent, Long id) {
    Category category = getCategoryFromCategoryContent(categoryContent);
    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        member, category, id);

    return favoriteOptional.isPresent();
  }
}
