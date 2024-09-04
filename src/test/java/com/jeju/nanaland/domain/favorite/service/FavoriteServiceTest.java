package com.jeju.nanaland.domain.favorite.service;

import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class FavoriteServiceTest {

  @InjectMocks
  FavoriteService favoriteService;

  @Mock
  FavoriteRepository favoriteRepository;
}