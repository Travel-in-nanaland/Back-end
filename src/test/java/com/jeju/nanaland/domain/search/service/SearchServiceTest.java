package com.jeju.nanaland.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.domain.experience.repository.ExperienceRepository;
import com.jeju.nanaland.domain.favorite.service.MemberFavoriteService;
import com.jeju.nanaland.domain.festival.repository.FestivalRepository;
import com.jeju.nanaland.domain.market.repository.MarketRepository;
import com.jeju.nanaland.domain.nana.repository.NanaRepository;
import com.jeju.nanaland.domain.nature.repository.NatureRepository;
import com.jeju.nanaland.domain.restaurant.repository.RestaurantRepository;
import com.jeju.nanaland.global.config.RedisConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @Test
  @DisplayName("검색어 정규화 테스트")
  void normalizeKeywordTest() {
    // given
    String keyword = "JEJU Jeju-city Korean_Restaurant";

    // when
    List<String> normalizedKeyword = Arrays.stream(keyword.split("\\s+"))  // 공백기준 분할
        .map(splittedKeyword -> splittedKeyword
            .replace("-", "")  // 하이픈 제거
            .replace("_", "")  // 언더스코어 제거
            .toLowerCase()  // 소문자로
        )
        .toList();

    // then
    assertThat(normalizedKeyword).containsExactly(
        "jeju",
        "jejucity",
        "koreanrestaurant"
    );
  }

  @Test
  @DisplayName("검색어 조합 테스트")
  void combinationUserKeywordsTest() {
    // given
    List<String> keywords = List.of("jeju", "city", "restaurant");

    // when
    List<String> combinedKeywords = new ArrayList<>(keywords);
    for (int i = 0; i < keywords.size() - 1; i++) {
      StringBuilder combinedKeyword = new StringBuilder();
      combinedKeyword.append(keywords.get(i));
      for (int j = i + 1; j < keywords.size(); j++) {
        combinedKeyword.append(keywords.get(j));
        combinedKeywords.add(combinedKeyword.toString());
      }
    }

    // then
    assertThat(combinedKeywords).containsExactly(
        "jeju",
        "city",
        "restaurant",
        "jejucity",
        "jejucityrestaurant",
        "cityrestaurant"
    );
  }

}