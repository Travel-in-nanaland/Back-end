package com.jeju.nanaland.domain.search.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.config.RedisConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


  @Test
  @DisplayName("전체 카테고리 검색")
  void searchAllTest() {
    // given
    Member member = Member.builder()
        .language(Language.KOREAN)
        .build();
    MemberInfoDto memberInfoDto = MemberInfoDto.builder()
        .member(member)
        .language(Language.KOREAN)
        .build();

    when(zSetOperations.incrementScore(any(String.class), any(String.class), any(Double.class)))
        .thenReturn(1.0);
    when(natureRepository.searchCompositeDtoByKeyword(any(String.class), any(Language.class), any(
        Pageable.class)))
        .thenReturn(Page.empty());
    when(festivalRepository.searchCompositeDtoByKeyword(any(String.class), any(Language.class), any(
        Pageable.class)))
        .thenReturn(Page.empty());
    when(marketRepository.searchCompositeDtoByKeyword(any(String.class), any(Language.class),
        any(Pageable.class)))
        .thenReturn(Page.empty());
    when(experienceRepository.searchCompositeDtoByKeyword(any(String.class), any(Language.class),
        any(Pageable.class)))
        .thenReturn(Page.empty());
    when(restaurantRepository.searchCompositeDtoByKeyword(any(String.class), any(Language.class),
        any(Pageable.class)))
        .thenReturn(Page.empty());
    when(nanaRepository.searchNanaThumbnailDtoByKeyword(any(String.class), any(Language.class),
        any(Pageable.class)))
        .thenReturn(Page.empty());
    when(memberFavoriteService.getFavoritePostIdsWithMember(any(Member.class)))
        .thenReturn(List.of());

    // when
    searchService.searchAll(memberInfoDto, "TEST");

    // then
  }
}