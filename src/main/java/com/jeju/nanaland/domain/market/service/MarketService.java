package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.common.service.PostService;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.market.dto.MarketResponse;
import com.jeju.nanaland.domain.market.dto.MarketResponse.MarketThumbnail;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.global.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

  private final MarketRepository marketRepository;
  private final FavoriteService favoriteService;

  public MarketResponse.MarketThumbnailDto getMarketList(Locale locale, int page, int size) {

    // default : page = 0, size = 12
    Pageable pageable = PageRequest.of(page, size);
    Page<MarketThumbnail> marketThumbnails = marketRepository.findMarketThumbnails(locale,
        pageable);

    List<MarketThumbnail> data = new ArrayList<>();
    for (MarketThumbnail marketThumbnail : marketThumbnails) {
      data.add(MarketThumbnail.builder()
          .id(marketThumbnail.getId())
          .title(marketThumbnail.getTitle())
          .thumbnailUrl(marketThumbnail.getThumbnailUrl())
          .addressTag(PostService.extractAddressTag(locale, marketThumbnail.getAddressTag()))
          .build());
    }

    return MarketResponse.MarketThumbnailDto.builder()
        .totalElements(marketThumbnails.getTotalElements())
        .data(data)
        .build();
  }

  @Transactional
  public String toggleLikeStatus(Member member, Long postId) {
    marketRepository.findById(postId)
        .orElseThrow(() -> new BadRequestException("해당 id의 전통시장 게시물이 존재하지 않습니다."));

    return favoriteService.toggleLikeStatus(member, CategoryContent.MARKET, postId);
  }
}
