package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

  private final FavoriteService favoriteService;

  @Transactional
  public String toggleLikeStatus(Member member, Long postId) {
    return favoriteService.toggleLikeStatus(member, CategoryContent.MARKET, postId);
  }
}
