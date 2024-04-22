package com.jeju.nanaland.domain.nature.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NatureService {

  private final NatureRepository natureRepository;
  private final FavoriteService favoriteService;

  @Transactional
  public FavoriteResponse.StatusDto toggleLikeStatus(Member member, Long postId) {
    natureRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 7대자연 게시물이 존재하지 않습니다."));

    Boolean status = favoriteService.toggleLikeStatus(member, CategoryContent.NATURE, postId);
    return FavoriteResponse.StatusDto.builder()
        .isFavorite(status)
        .build();
  }
}
