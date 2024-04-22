package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse.StatusDto;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

  private final MarketRepository marketRepository;
  private final FavoriteService favoriteService;

  @Transactional
  public StatusDto toggleLikeStatus(Member member, Long postId) {
    marketRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 전통시장 게시물이 존재하지 않습니다."));

    Boolean status = favoriteService.toggleLikeStatus(member, CategoryContent.MARKET, postId);
    return FavoriteResponse.StatusDto.builder()
        .isFavorite(status)
        .build();
  }
}
