package com.jeju.nanaland.domain.search.service;

import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.config.RedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
@Import(RedisConfig.class)
class SearchServiceTest {

  @InjectMocks
  private SearchService searchService;

  @Mock
  private NanaRepository nanaRepository;
  @Mock
  private NatureRepository natureRepository;
  @Mock
  private ExperienceRepository experienceRepository;
  @Mock
  private MarketRepository marketRepository;
  @Mock
  private FestivalRepository festivalRepository;
  @Mock
  private RestaurantRepository restaurantRepository;
  @Mock
  private MemberFavoriteService memberFavoriteService;
  @Mock
  private RedisTemplate<String, String> redisTemplate; // Mock 객체 생성
  @Mock
  private ZSetOperations<String, String> zSetOperations; // ZSetOperations mock

  @BeforeEach
  public void setup() {
    // opsForZSet() 호출 시 ZSetOperations mock을 반환하도록 설정
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
  }

}