package com.jeju.nanaland.domain.experience.service;

import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.repository.CategoryRepository;
import com.jeju.nanaland.domain.favorite.entity.Favorite;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.global.exception.ServerErrorException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperienceService {

  private final CategoryRepository categoryRepository;
  private final FavoriteRepository favoriteRepository;

  @Transactional
  public String toggleLikeStatus(Member member, Long postId) {
    Category experienceCategory = categoryRepository.findByContent("EXPERIENCE")
        .orElseThrow(() -> new ServerErrorException("EXPERIENCE에 해당하는 카테고리가 없습니다."));

    Optional<Favorite> favoriteOptional = favoriteRepository.findByMemberAndCategoryAndPostId(
        member, experienceCategory, postId);

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
          .category(experienceCategory)
          .postId(postId)
          .build();

      // 좋아요 추가
      favoriteRepository.save(favorite);
      return "좋아요 추가";
    }
  }
}
