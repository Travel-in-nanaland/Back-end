package com.jeju.nanaland.domain.market.service;

import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

  @InjectMocks
  MarketService marketService;

  @Mock
  MarketRepository marketRepository;
  @Mock
  FavoriteRepository favoriteRepository;

  @Test
  @DisplayName("전통시장 썸네일 페이징")
  void marketThumbnailPagingTest() {

  }

}